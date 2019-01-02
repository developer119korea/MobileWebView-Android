package unitydirectionkit.mobilewebview;

import android.view.Surface;

public abstract interface GLWebView
{
    public abstract void setSurface(Surface paramSurface);

    public abstract void setData(String paramString);

    public abstract void setUrl(String paramString);

    public abstract String getUrl();

    public abstract void start();

    public abstract void stop();

    public abstract void release();

    public abstract boolean moveForward();

    public abstract boolean moveBack();

    public abstract void showKeyboard(boolean paramBoolean);

    public abstract void setInputText(String paramString);

    public abstract void callFunction(String functionName, String... args);

    public abstract void pageClickTo(float paramFloat1, float paramFloat2);

    public abstract void pageScrollBy(float paramFloat1, float paramFloat2);

    public abstract int contentHeight();

    public abstract void setOnEventListener(OnEventListener paramOnEventListener);

    public static enum WebStates
    {
        Empty,  Prepared,  Started,  Loading,  Finished,  Error,  HttpError,  ElementReceived;

        private WebStates() {}
    }

    public static class WebState
    {
        private GLWebView.WebStates mState;
        private float mValueFloat;
        private long mValueLong;
        private String mValueString;

        public WebState(GLWebView.WebStates state)
        {
            this.mState = state;
            this.mValueFloat = -1.0F;
            this.mValueLong = -1L;
            this.mValueString = "";
        }

        public WebState(GLWebView.WebStates state, float floatValue)
        {
            this.mState = state;
            this.mValueFloat = floatValue;
            this.mValueLong = -1L;
            this.mValueString = "";
        }

        public WebState(GLWebView.WebStates state, long longValue)
        {
            this.mState = state;
            this.mValueFloat = -1.0F;
            this.mValueLong = longValue;
            this.mValueString = "";
        }

        public WebState(GLWebView.WebStates state, String stringValue)
        {
            this.mState = state;
            this.mValueFloat = -1.0F;
            this.mValueLong = -1L;
            this.mValueString = stringValue;
        }

        public GLWebView.WebStates GetState()
        {
            return this.mState;
        }

        public float GetFloatValue()
        {
            return this.mValueFloat;
        }

        public long GetLongValue()
        {
            return this.mValueLong;
        }

        public String GetStringValue()
        {
            return this.mValueString;
        }
    }

    public static abstract interface OnEventListener
    {
        public abstract void onEventListener(GLWebView.WebStates paramWebStates, Object paramObject);
    }
}
