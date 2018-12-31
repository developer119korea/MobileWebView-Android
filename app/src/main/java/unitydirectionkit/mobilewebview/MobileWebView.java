package unitydirectionkit.mobilewebview;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import com.unity3d.player.UnityPlayer;
import java.util.LinkedList;
import java.util.Queue;

public class MobileWebView
        implements SurfaceTexture.OnFrameAvailableListener, GLWebView.OnEventListener
{
    private Activity mActivity;
    private int mSurfaceTexturePointer;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private float[] mSurfaceTransformMatrix;
    private int mFramesCounter;
    private GLWebView mWebView;
    private boolean mWebViewReady;
    private int mNativeIndex;
    private int mWidth;
    private int mHeight;
    private GLWebView.WebState mWebViewState;
    private Queue<GLWebView.WebState> mWebViewStates;

    private static enum NativeCallEvents
    {
        Start,  Update;

        private NativeCallEvents() {}
    }

    static
    {
        System.loadLibrary("MobileWebView");
    }

    public MobileWebView(int index, int width, int height)
    {
        this.mActivity = UnityPlayer.currentActivity;
        this.mWidth = width;
        this.mHeight = height;

        nativeInit(index);
        this.mNativeIndex = index;
        this.mSurfaceTransformMatrix = new float[16];
        this.mWebViewStates = new LinkedList();

        final MobileWebView mobileWebView = this;
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                MobileWebView.this.mWebView = new GLWebViewNative(MobileWebView.this.mActivity);
                MobileWebView.this.mWebView.setOnEventListener(mobileWebView);
                ViewGroup viewGroup = (ViewGroup)MobileWebView.this.mActivity.findViewById(16908290);
                if ((MobileWebView.this.mWidth <= 0) && (MobileWebView.this.mHeight <= 0)) {
                    viewGroup.addView((View)MobileWebView.this.mWebView);
                } else {
                    viewGroup.addView((View)MobileWebView.this.mWebView, MobileWebView.this.mWidth, MobileWebView.this.mHeight);
                }
                MobileWebView.this.mWebViewReady = true;
            }
        });
    }

    private SurfaceTexture getSurfaceTexture(int surfacePointer)
    {
        SurfaceTexture sTexture = new SurfaceTexture(surfacePointer);
        sTexture.setDefaultBufferSize(this.mWidth, this.mHeight);
        sTexture.setOnFrameAvailableListener(this);
        return sTexture;
    }

    public void nativeCallHandler(int eventID)
    {
        NativeCallEvents event = NativeCallEvents.values()[eventID];
        if (event == NativeCallEvents.Update) {
            exportUpdateSurfaceTexture();
        } else if (event == NativeCallEvents.Start) {
            exportStartRender();
        }
    }

    public void exportUpdateSurfaceTexture()
    {
        synchronized (this)
        {
            this.mSurfaceTexture.updateTexImage();
            this.mSurfaceTexture.getTransformMatrix(this.mSurfaceTransformMatrix);
            setSurfaceTextureMatrix(this.mNativeIndex, this.mSurfaceTransformMatrix);
        }
    }

    public void exportSetUrl(String path)
    {
        if (this.mWebView != null) {
            this.mWebView.setUrl(path);
        }
    }

    public String exportGetUrl()
    {
        if (this.mWebView != null) {
            return this.mWebView.getUrl();
        }
        return "";
    }

    public void exportSetData(String data)
    {
        if (this.mWebView != null) {
            this.mWebView.setData(data);
        }
    }

    public boolean exportStartRender()
    {
        if (this.mWebView == null) {
            return false;
        }
        try
        {
            if (this.mSurfaceTexture == null)
            {
                this.mSurfaceTexturePointer = genSurfaceTexturePointer(this.mNativeIndex);
                this.mSurfaceTexture = getSurfaceTexture(this.mSurfaceTexturePointer);
                this.mSurface = new Surface(this.mSurfaceTexture);
                this.mWebView.setSurface(this.mSurface);
            }
        }
        catch (Exception e)
        {
            Log.e("Unity", "Error when call start render method: " + e.toString());
            return false;
        }
        this.mWebView.start();
        return true;
    }

    public void exportStopRender()
    {
        if (this.mWebView == null) {
            return;
        }
        this.mWebView.stop();
        this.mWebView.setSurface(null);
        if (this.mSurfaceTexture != null)
        {
            this.mSurfaceTexture.release();
            this.mSurfaceTexture = null;
        }
        if (this.mSurface != null)
        {
            this.mSurface.release();
            this.mSurface = null;
        }
        this.mFramesCounter = 0;
    }

    public void exportRelease()
    {
        exportStopRender();
        if (this.mWebView != null) {
            this.mWebView.release();
        }
        this.mActivity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                ViewGroup vg = (ViewGroup)((View)MobileWebView.this.mWebView).getParent();
                vg.removeView((View)MobileWebView.this.mWebView);
            }
        });
        this.mWebViewReady = false;
    }

    public boolean exportIsViewReady()
    {
        return this.mWebViewReady;
    }

    public boolean exportMoveForward()
    {
        if (this.mWebView != null) {
            return this.mWebView.moveForward();
        }
        return false;
    }

    public boolean exportMoveBack()
    {
        if (this.mWebView != null) {
            return this.mWebView.moveBack();
        }
        return false;
    }

    public void exportShowKeyboard(boolean state)
    {
        if (this.mWebView != null) {
            this.mWebView.showKeyboard(state);
        }
    }

    public void exportSetInputText(String text)
    {
        if (this.mWebView != null) {
            this.mWebView.setInputText(text);
        }
    }

    public void exportCallFunction(String functionName)
    {
        if (this.mWebView != null) {
            this.mWebView.callFunction(functionName);
        }
    }

    public void exportPageClickTo(int x, int y)
    {
        if (this.mWebView != null) {
            this.mWebView.pageClickTo(x, y);
        }
    }

    public void exportPageScrollBy(int x, int y)
    {
        if (this.mWebView != null) {
            this.mWebView.pageScrollBy(x, y);
        }
    }

    public int exportFramesCounter()
    {
        return this.mFramesCounter;
    }

    public int exportContentHeight()
    {
        if (this.mWebView != null) {
            return this.mWebView.contentHeight();
        }
        return 0;
    }

    public int exportGetState()
    {
        synchronized (this.mWebViewStates)
        {
            if (this.mWebViewStates.size() > 0)
            {
                this.mWebViewState = ((GLWebView.WebState)this.mWebViewStates.poll());
                return this.mWebViewState.GetState().ordinal();
            }
            return GLWebView.WebStates.Empty.ordinal();
        }
    }

    public float exportGetStateFloatValue()
    {
        synchronized (this.mWebViewStates)
        {
            if (this.mWebViewState != null) {
                return this.mWebViewState.GetFloatValue();
            }
            return -1.0F;
        }
    }

    public long exportGetStateLongValue()
    {
        synchronized (this.mWebViewStates)
        {
            if (this.mWebViewState != null) {
                return this.mWebViewState.GetLongValue();
            }
            return -1L;
        }
    }

    public String exportGetStateStringValue()
    {
        synchronized (this.mWebViewStates)
        {
            if (this.mWebViewState != null) {
                return this.mWebViewState.GetStringValue();
            }
            return "";
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        synchronized (this)
        {
            this.mFramesCounter += 1;
        }
    }

    public void onEventListener(GLWebView.WebStates eventType, Object value)
    {
        synchronized (this.mWebViewStates)
        {
            switch (eventType)
            {
                case Started:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.Started, (String)value));
                    break;
                case Loading:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.Loading, ((Integer)value).intValue()));
                    break;
                case Finished:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.Finished, (String)value));
                    break;
                case Error:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.Error, ((Integer)value).intValue()));
                    break;
                case HttpError:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.HttpError));
                    break;
                case ElementReceived:
                    this.mWebViewStates.add(new GLWebView.WebState(GLWebView.WebStates.ElementReceived, (String)value));
            }
        }
    }

    private native void nativeInit(int paramInt);

    private native void clearMediaPlayerTexture(int paramInt);

    private native int genSurfaceTexturePointer(int paramInt);

    private native void setSurfaceTextureMatrix(int paramInt, float[] paramArrayOfFloat);
}
