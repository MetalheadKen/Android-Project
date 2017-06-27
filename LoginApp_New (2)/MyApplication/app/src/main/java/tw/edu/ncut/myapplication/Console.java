package tw.edu.ncut.myapplication;

import android.app.Activity;
import android.text.Html;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Created by user on 2016/11/25.
 */
public class Console {
    public Activity activity;
    public TextView textview;

    public Console(TextView paramTextView) {
        if (paramTextView != null) {
            this.textview = paramTextView;
            paramTextView.setMovementMethod(new ScrollingMovementMethod());
            this.activity = ((Activity) paramTextView.getContext());
        }
    }

    public void clear() {
        this.textview.setText("");
    }

    public void output(String paramString) {
        if (this.textview == null) {
            return;
        }
        this.activity.runOnUiThread(new TextViewOutput(this.textview, paramString));
    }
}

class TextViewOutput implements Runnable {
    public TextView console;
    public String message;

    public TextViewOutput(TextView paramTextView, String paramString) {
        this.console = paramTextView;
        this.message = paramString;
    }

    public void run() {
        this.console.append(Html.fromHtml(this.message + "<br>"));
        Layout localLayout = this.console.getLayout();
        if (localLayout != null) {
            int i = localLayout.getLineBottom(this.console.getLineCount() - 1)
                    - this.console.getScrollY() - this.console.getHeight();
            if (i > 0) {
                this.console.scrollBy(0, i);
            }
        } else {
            return;
        }
        this.console.scrollTo(0, 0);
    }
}
