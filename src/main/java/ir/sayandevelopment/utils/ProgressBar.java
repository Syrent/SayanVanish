package ir.sayandevelopment.utils;

import com.google.common.base.Strings;

public class ProgressBar {
    public static String progressBar(int current, int max, int total, String completeString, String notCompleteString) {
        float percent = (float) current / max;
        int progressBars = (int) (total * percent);

        return Strings.repeat(completeString, progressBars) + Strings.repeat(notCompleteString, total - progressBars);
    }
}
