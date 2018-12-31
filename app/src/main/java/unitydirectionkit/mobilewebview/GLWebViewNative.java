package unitydirectionkit.mobilewebview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import android.os.Build;

public class GLWebViewNative
        extends WebView
        implements GLWebView
{
    private Activity mActivity;
    private Surface mWebSurface;
    private Canvas mWebSurfaceCanvas;
    private String mPageUrl;
    private boolean mIsDrawing;
    private boolean mPageStarted;
    private boolean mPageFinished;
    private float mDensity;
    private GLWebView.OnEventListener mOnEventListener;

    public GLWebViewNative(Activity activity)
    {
        super(activity);

        this.mActivity = activity;
        this.mPageFinished = true;
        this.mDensity = getResources().getDisplayMetrics().density;

        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setAppCacheEnabled(true);

        setWebViewClient(new WebViewClient()
        {
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);

                GLWebViewNative.this.mPageStarted = true;
                GLWebViewNative.this.mPageUrl = url;
                if ((GLWebViewNative.this.mOnEventListener != null) && (GLWebViewNative.this.mPageFinished))
                {
                    GLWebViewNative.this.mPageFinished = false;
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.Started, GLWebViewNative.this.mPageUrl);
                }
            }

            public void onReceivedError(WebView view, int errorCod, String description, String failingUrl)
            {
                if (GLWebViewNative.this.mOnEventListener != null) {
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.Error, Integer.valueOf(Math.abs(errorCod)));
                }
            }

            @TargetApi(23)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
                super.onReceivedError(view, request, error);
                int errorCode = -1;
                if (Build.VERSION.SDK_INT >= 23) {
                    errorCode = error.getErrorCode();
                }
                if (GLWebViewNative.this.mOnEventListener != null) {
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.Error, Integer.valueOf(Math.abs(errorCode)));
                }
            }

            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
            {
                super.onReceivedHttpError(view, request, errorResponse);
                if (GLWebViewNative.this.mOnEventListener != null) {
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.HttpError, null);
                }
            }

            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                GLWebViewNative.this.mPageStarted = false;
                GLWebViewNative.this.mPageFinished = true;
                if (GLWebViewNative.this.mOnEventListener != null) {
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.Finished, url);
                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                String scheme = Uri.parse(url).getScheme();
                if ((!scheme.equals("https")) && (!scheme.equals("http"))) {
                    try
                    {
                        GLWebViewNative.this.mActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                        return true;
                    }
                    catch (ActivityNotFoundException e)
                    {
                        Log.d("Unity", e.toString());
                        return false;
                    }
                }
                return false;
            }

            @TargetApi(21)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                if (Build.VERSION.SDK_INT >= 21)
                {
                    String scheme = request.getUrl().getScheme();
                    if ((!scheme.equals("https")) && (!scheme.equals("http"))) {
                        try
                        {
                            GLWebViewNative.this.mActivity.startActivity(new Intent("android.intent.action.VIEW", request.getUrl()));
                            return true;
                        }
                        catch (ActivityNotFoundException e)
                        {
                            Log.e("Unity", e.toString());
                            return false;
                        }
                    }
                }
                return false;
            }
        });
        setWebChromeClient(new WebChromeClient()
        {
            public void onReceivedTitle(WebView view, String title)
            {
                super.onReceivedTitle(view, title);
            }

            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                if ((!GLWebViewNative.this.mPageFinished) && (newProgress < 100) &&
                        (GLWebViewNative.this.mPageStarted) && (GLWebViewNative.this.mOnEventListener != null)) {
                    GLWebViewNative.this.mOnEventListener.onEventListener(GLWebView.WebStates.Loading, Integer.valueOf(newProgress));
                }
            }
        });
        addJavascriptInterface(this, WebViewScripts.JS_CALL_BRIDGE);
    }

    private void hideNavBar(Activity activity)
    {
        View decorView = activity.getWindow().getDecorView();

        int uiOptions = 2;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void draw(Canvas canvas)
    {
        if ((this.mWebSurface == null) || (!this.mIsDrawing)) {
            return;
        }
        this.mWebSurfaceCanvas = null;
        try
        {
            this.mWebSurfaceCanvas = this.mWebSurface.lockCanvas(null);
            if (this.mWebSurfaceCanvas != null)
            {
                this.mWebSurfaceCanvas.translate(-getScrollX(), -getScrollY());
                super.draw(this.mWebSurfaceCanvas);
                this.mWebSurface.unlockCanvasAndPost(this.mWebSurfaceCanvas);
            }
        }
        catch (Exception e)
        {
            Log.e("Unity", "Error while rendering web view to surface: " + e);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        return false;
    }

    @JavascriptInterface
    public void onDataReceived(String key, String value)
    {
        if ((key.equals("elementData")) &&
                (value != null) && (!value.isEmpty()) &&
                (this.mOnEventListener != null)) {
            this.mOnEventListener.onEventListener(GLWebView.WebStates.ElementReceived, value);
        }
    }

    public void start()
    {
        this.mIsDrawing = true;
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.onResume();
            }
        });
    }

    public void stop()
    {
        this.mIsDrawing = false;
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.onPause();
            }
        });
    }

    public void release()
    {
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.destroy();
            }
        });
    }

    public void setOnEventListener(GLWebView.OnEventListener listener)
    {
        this.mOnEventListener = listener;
    }

    public void setSurface(Surface surface)
    {
        this.mWebSurface = surface;
    }

    public void setUrl(String url)
    {
        this.mPageUrl = url;
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.loadUrl(GLWebViewNative.this.mPageUrl);
            }
        });
    }

    public String getUrl()
    {
        return this.mPageUrl;
    }

    public void setData(final String data)
    {
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
            }
        });
    }

    public boolean moveForward()
    {
        FutureTask<Boolean> moveForward = new FutureTask(new Callable()
        {
            public Boolean call()
                    throws Exception
            {
                boolean res = GLWebViewNative.this.canGoForward();
                if (res) {
                    GLWebViewNative.this.goForward();
                }
                return Boolean.valueOf(res);
            }
        });
        this.mActivity.runOnUiThread(moveForward);
        try
        {
            return ((Boolean)moveForward.get()).booleanValue();
        }
        catch (InterruptedException|ExecutionException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean moveBack()
    {
        FutureTask<Boolean> moveBack = new FutureTask(new Callable()
        {
            public Boolean call()
                    throws Exception
            {
                boolean res = GLWebViewNative.this.canGoBack();
                if (res) {
                    GLWebViewNative.this.goBack();
                }
                return Boolean.valueOf(res);
            }
        });
        this.mActivity.runOnUiThread(moveBack);
        try
        {
            return ((Boolean)moveBack.get()).booleanValue();
        }
        catch (InterruptedException|ExecutionException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void showKeyboard(boolean state)
    {
        setFocusable(state);
    }

    public void setInputText(final String text)
    {
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                GLWebViewNative.this.loadUrl(WebViewScripts.setInputText(text));
            }
        });
    }

    public void pageClickTo(final float x, final float y)
    {
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100L;

                MotionEvent e = MotionEvent.obtain(downTime, eventTime, 0, x, y, 0);
                GLWebViewNative.this.onTouchEvent(e);

                e = MotionEvent.obtain(downTime, eventTime, 1, x, y, 0);
                GLWebViewNative.this.onTouchEvent(e);

                GLWebViewNative.this.loadUrl(WebViewScripts.elementData(x / GLWebViewNative.this.mDensity, y / GLWebViewNative.this.mDensity));
            }
        });
    }

    public void pageScrollBy(final float x, final float y)
    {
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                int yPos = (int)y;
                if (GLWebViewNative.this.getScrollY() + y <= 0.0F) {
                    yPos = -GLWebViewNative.this.getScrollY();
                }
                if (GLWebViewNative.this.getScrollY() + y >= GLWebViewNative.this.contentHeight()) {
                    yPos = GLWebViewNative.this.contentHeight() - GLWebViewNative.this.getScrollY();
                }
                GLWebViewNative.this.scrollBy((int)x, yPos);
            }
        });
    }

    public int contentHeight()
    {
        return (int)(getContentHeight() * this.mDensity) - getWidth();
    }
}
