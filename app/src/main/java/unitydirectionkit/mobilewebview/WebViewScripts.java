package unitydirectionkit.mobilewebview;

public class WebViewScripts
{
    public static String DATA_SEPARATOR = "@";
    public static String JS_CALL_BRIDGE = "jsBridge";

    public static String elementData(float x, float y)
    {
        return "javascript:(function(){elem = document.elementFromPoint(" + x + "," + y + ");" + JS_CALL_BRIDGE + ".onDataReceived('elementData', elem.tagName + '" + DATA_SEPARATOR + "' + elem.value + '" + DATA_SEPARATOR + "' + document.activeElement.name);})()";
    }

    public static String clientHeight()
    {
        return "javascript:(function(){var h = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;" + JS_CALL_BRIDGE + ".onDataReceived('clientHeight', h);})()";
    }

    public static String clientWidth()
    {
        return "javascript:(function(){var w = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;" + JS_CALL_BRIDGE + ".onDataReceived('clientWidth', w);})()";
    }

    public static String clearFocus()
    {
        return "javascript:(function(){elem = document.activeElement;elem.blur();})()";
    }

    public static String setInputText(String text)
    {
        return "javascript:(function(){elem = document.activeElement;elem.value = '" + text + "';})()";
    }

    public static String clickTo(float x, float y)
    {
        return "javascript:(function(){ev = document.createEvent('MouseEvent');ev.initMouseEvent('click', true, true, window, null, 0, 0," + x + "," + y + ", false, false, false, false, 0, null);elem = document.elementFromPoint(" + x + "," + y + ");elem.dispatchEvent(ev);elem.focus();})()";
    }

    public static String scrollBy(float x, float y)
    {
        return "javascript:(function(){window.scrollBy(" + x + "," + y + ");})()";
    }
}
