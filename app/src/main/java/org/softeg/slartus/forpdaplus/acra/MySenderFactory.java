package org.softeg.slartus.forpdaplus.acra;

import android.content.Context;

import com.google.auto.service.AutoService;

import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.sender.ReportSenderFactory;
import org.jetbrains.annotations.NotNull;

@AutoService(ReportSenderFactory.class)
public class MySenderFactory implements ReportSenderFactory {
    @NotNull
    @Override
    public ReportSender create(@NotNull Context context, @NotNull CoreConfiguration coreConfiguration) {
        return new AcraPostSender();
    }

    public static class AcraPostSender implements ReportSender {
        @Override
        public void send(@NotNull Context context, @NotNull CrashReportData errorContent) throws ReportSenderException {
            ACRAPostSender.send(errorContent);
        }

    }
}

