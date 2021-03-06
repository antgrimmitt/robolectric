package org.robolectric.bytecode;

import android.app.Application;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.internal.TestLifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomRobolectricTestRunnerTest {
    public static Method beforeTestMethod;
    public static Method afterTestMethod;
    private Result result = new Result();

    @Before
    public void setUp() throws Exception {
        beforeTestMethod = null;
        afterTestMethod = null;
    }

    @Test
    public void shouldInitializeApplication() throws Exception {
        new CustomRobolectricTestRunner(TestApplicationIsInitialized.class).run(new MyRunNotifier(result));
        assertNoFailures();
        assertThat(result.getRunCount()).isEqualTo(1);
    }

    @Ignore("should only be run by custom test runner")
    public static class TestApplicationIsInitialized {
        @Test public void shouldPass() throws Exception {
            assertNotNull(Robolectric.application);
            assertEquals(CustomApplication.class, Robolectric.application.getClass());
        }
    }

    @Test
    public void shouldInvokePrepareTestWithAnInstanceOfTheTest() throws Exception {
        new CustomRobolectricTestRunner(TestTestIsPrepared.class).run(new MyRunNotifier(result));
        assertNoFailures();
        assertThat(result.getRunCount()).isEqualTo(1);
    }

    @Ignore("should only be run by custom test runner")
    public static class TestTestIsPrepared {
        boolean prepared = false;

        @SuppressWarnings("UnusedDeclaration")
        public void prepare() {
            prepared = true;
        }

        @Test public void shouldPass() throws Exception {
            assertEquals(true, prepared);
            assertEquals(AsmInstrumentingClassLoader.class.getName(), this.getClass().getClassLoader().getClass().getName());
        }
    }

    @Test
    public void shouldInvokeBeforeTestWithTheCorrectMethod() throws Exception {
        CustomRobolectricTestRunner testRunner = new CustomRobolectricTestRunner(TestBeforeAndAfter.class);
        testRunner.run(new MyRunNotifier(result));
        assertNoFailures();
        assertThat(result.getRunCount()).isEqualTo(1);
        assertThat(beforeTestMethod.getName()).isEqualTo("properMethodName");
        assertThat(afterTestMethod.getName()).isEqualTo("properMethodName");
        assertEquals(AsmInstrumentingClassLoader.class, beforeTestMethod.getDeclaringClass().getClassLoader().getClass());
        assertEquals(AsmInstrumentingClassLoader.class, afterTestMethod.getDeclaringClass().getClassLoader().getClass());
    }

    @Ignore("should only be run by custom test runner")
    public static class TestBeforeAndAfter {
        @Test
        public void properMethodName() throws Exception {
        }
    }

    public static class CustomRobolectricTestRunner extends TestRunners.WithDefaults {
        public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override public Setup createSetup() {
            return new Setup() {
                @Override public boolean shouldAcquire(String name) {
                    return !name.equals(CustomRobolectricTestRunnerTest.class.getName()) && super.shouldAcquire(name);
                }
            };
        }

        @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
            return MyTestLifecycle.class;
        }

        public static class MyTestLifecycle extends DefaultTestLifecycle {
            @Override public void prepareTest(Object test) {
                try {
                    Method method = test.getClass().getMethod("prepare");
                    method.invoke(test);
                } catch (NoSuchMethodException e) {
                    // no prob
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override public void beforeTest(Method method) {
                beforeTestMethod = method;
            }

            @Override public void afterTest(Method method) {
                afterTestMethod = method;
            }

            @Override public Application createApplication(Method method, AndroidManifest appManifest) {
                return new CustomApplication();
            }
        }
    }

    public static class CustomApplication extends Application {
    }

    private static class MyRunNotifier extends RunNotifier {
        public MyRunNotifier(Result result) {
            addListener(result.createListener());
        }
    }

    private void assertNoFailures() {
        if (!result.wasSuccessful()) {
            for (Failure failure : result.getFailures()) {
                fail(failure.getMessage(), failure.getException());
            }
        }
    }
}
