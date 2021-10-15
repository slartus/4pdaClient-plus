package org.softeg.slartus.forpdaplus;

import android.app.Application;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.UiThreadTestRule;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ApplicationTestRule<T extends Application> extends UiThreadTestRule {
    Class<T> appClazz;
    boolean wait = false;
    T app;

    public ApplicationTestRule(Class<T> applicationClazz) {
        this(applicationClazz, false);
    }

    public ApplicationTestRule(Class<T> applicationClazz, boolean wait) {
        this.appClazz = applicationClazz;
        this.wait = wait;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new ApplicationStatement(super.apply(base, description));
    }

    private void terminateApp() {
        if (app != null) {
            app.onTerminate();
        }
    }

    public void createApplication() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        app = (T) InstrumentationRegistry.getInstrumentation().newApplication(this.getClass().getClassLoader(), appClazz.getName(), InstrumentationRegistry.getInstrumentation().getTargetContext());
        InstrumentationRegistry.getInstrumentation().callApplicationOnCreate(app);
    }

    private class ApplicationStatement extends Statement {

        private final Statement mBase;

        public ApplicationStatement(Statement base) {
            mBase = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                if (!wait) {
                    createApplication();
                }
                mBase.evaluate();
            } finally {
                terminateApp();
                app = null;
            }
        }
    }
}