package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Common Selenium 4 helpers for Java 21.
 * Place at src/test/java/utils/SeleniumUtils.java.
 *
 * Usage: SeleniumUtils ui = new SeleniumUtils(driver, Duration.ofSeconds(10));
 *
 * Create one driver and helper per test/scenario. Do not share across threads.
 * Keep implicit waits at zero; this class uses explicit waits.
 * Assertions and business workflows belong in tests/page objects.
 * This helper does not create or quit the driver: the test or hooks own it.
 * Failures propagate to the test; actions are not blindly retried.
 */
public final class SeleniumUtils {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumUtils(WebDriver driver) {
        this(driver, Duration.ofSeconds(10));
    }

    public SeleniumUtils(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "driver");
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        this.wait = new WebDriverWait(driver, timeout);
    }

    // NAVIGATION
    public void open(String url) { driver.get(url); }
    public void back() { driver.navigate().back(); }
    public void forward() { driver.navigate().forward(); }
    public void refresh() { driver.navigate().refresh(); }
    public String title() { return driver.getTitle(); }
    public String currentUrl() { return driver.getCurrentUrl(); }
    public void maximize() { driver.manage().window().maximize(); }

    // WAITS: TimeoutException is deliberately allowed to fail the test.
    public <T> T waitUntil(ExpectedCondition<T> condition) {
        return wait.until(condition);
    }

    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Clickable means visible and enabled; an overlay may still intercept a click. */
    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Succeeds when the matching element is hidden or absent. */
    public void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    public void waitForTitle(String expectedTitle) {
        wait.until(ExpectedConditions.titleIs(expectedTitle));
    }

    public void waitForStale(WebElement oldElement) {
        wait.until(ExpectedConditions.stalenessOf(oldElement));
    }

    // ELEMENT ACTIONS
    public void click(By locator) { waitForClickable(locator).click(); }

    /** Replaces existing text in an editable input or textarea. */
    public void type(By locator, String text) {
        WebElement element = waitForClickable(locator);
        element.clear();
        element.sendKeys(text);
    }

    /** Appends text or sends Keys.ENTER, Keys.TAB, etc. */
    public void sendKeys(By locator, CharSequence... keys) {
        waitForClickable(locator).sendKeys(keys);
    }

    public void clear(By locator) { waitForClickable(locator).clear(); }
    public String getText(By locator) { return waitForVisible(locator).getText(); }

    /** Reads the current input value, including changes made by JavaScript. */
    public String getValue(By locator) {
        return waitForPresence(locator).getDomProperty("value");
    }

    public String getDomAttribute(By locator, String name) {
        return waitForPresence(locator).getDomAttribute(name);
    }

    public String getDomProperty(By locator, String name) {
        return waitForPresence(locator).getDomProperty(name);
    }

    public String getCssValue(By locator, String name) {
        return waitForVisible(locator).getCssValue(name);
    }

    /** Immediate snapshot (no explicit wait); zero matches returns an empty list. */
    public List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    public List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** Immediate visibility check. For delayed elements use waitForVisible instead. */
    public boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) { return waitForPresence(locator).isEnabled(); }
    public boolean isSelected(By locator) { return waitForPresence(locator).isSelected(); }

    /** For native checkboxes; custom widgets need application-specific handling. */
    public void setCheckbox(By locator, boolean selected) {
        WebElement checkbox = waitForClickable(locator);
        if (checkbox.isSelected() != selected) {
            checkbox.click();
        }
        wait.until(ExpectedConditions.elementSelectionStateToBe(locator, selected));
    }

    // NATIVE HTML SELECT MENUS ONLY. Custom dropdowns use ordinary clicks.
    public void selectByText(By locator, String text) {
        new Select(waitForClickable(locator)).selectByVisibleText(text);
    }

    public void selectByValue(By locator, String value) {
        new Select(waitForClickable(locator)).selectByValue(value);
    }

    public void selectByIndex(By locator, int index) {
        new Select(waitForClickable(locator)).selectByIndex(index);
    }

    public String selectedOptionText(By locator) {
        return new Select(waitForVisible(locator)).getFirstSelectedOption().getText();
    }

    public void deselectAll(By locator) {
        new Select(waitForClickable(locator)).deselectAll();
    }

    // MOUSE / KEYBOARD
    public void hover(By locator) {
        new Actions(driver).moveToElement(waitForVisible(locator)).perform();
    }

    public void doubleClick(By locator) {
        new Actions(driver).doubleClick(waitForClickable(locator)).perform();
    }

    public void rightClick(By locator) {
        new Actions(driver).contextClick(waitForClickable(locator)).perform();
    }

    /** Some custom HTML5 drag/drop widgets require application-specific handling. */
    public void dragAndDrop(By source, By target) {
        new Actions(driver).dragAndDrop(waitForVisible(source), waitForVisible(target)).perform();
    }

    // FRAMES
    public void switchToFrame(By locator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    public void switchToParentFrame() { driver.switchTo().parentFrame(); }
    public void switchToDefaultContent() { driver.switchTo().defaultContent(); }

    // NATIVE JAVASCRIPT ALERTS (not HTML modals)
    public Alert waitForAlert() { return wait.until(ExpectedConditions.alertIsPresent()); }
    public String alertText() { return waitForAlert().getText(); }
    public void acceptAlert() { waitForAlert().accept(); }
    public void dismissAlert() { waitForAlert().dismiss(); }

    public void answerPrompt(String text) {
        Alert alert = waitForAlert();
        alert.sendKeys(text);
        alert.accept();
    }

    // WINDOWS / TABS
    public String windowHandle() { return driver.getWindowHandle(); }
    public Set<String> windowHandles() { return Set.copyOf(driver.getWindowHandles()); }
    public void switchToWindow(String handle) { driver.switchTo().window(handle); }

    public void waitForWindowCount(int count) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(count));
    }

    /**
     * Capture windowHandles() BEFORE clicking the link that opens a window.
     * Waits for exactly one additional handle; fails if multiple new windows appear.
     * Returns the new handle and switches to it. Window handle order is undefined.
     */
    public String switchToNewWindow(Set<String> previousHandles) {
        Set<String> before = Set.copyOf(previousHandles);
        String handle = wait.until(d -> {
            List<String> added = d.getWindowHandles().stream()
                .filter(h -> !before.contains(h)).toList();
            if (added.size() > 1) {
                throw new IllegalStateException("Multiple new windows; select a handle explicitly");
            }
            return added.isEmpty() ? null : added.get(0);
        });
        driver.switchTo().window(handle);
        return handle;
    }

    public String openNewTab() {
        driver.switchTo().newWindow(WindowType.TAB);
        return driver.getWindowHandle();
    }

    /** Close only the child tab, then explicitly restore a known parent handle. */
    public void closeCurrentWindowAndSwitchTo(String remainingHandle) {
        if (driver.getWindowHandle().equals(remainingHandle)
                || !driver.getWindowHandles().contains(remainingHandle)) {
            throw new IllegalArgumentException("Supply a different, existing window handle");
        }
        driver.close();
        driver.switchTo().window(remainingHandle);
    }

    // JAVASCRIPT: use normal WebDriver interactions first.
    public Object executeScript(String script, Object... arguments) {
        return ((JavascriptExecutor) driver).executeScript(script, arguments);
    }

    public void scrollIntoView(By locator) {
        executeScript("arguments[0].scrollIntoView({block:'center',inline:'nearest'});",
            waitForPresence(locator));
    }

    public void scrollBy(int x, int y) {
        executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    /** Explicit escape hatch. JS click bypasses normal user interaction checks. */
    public void clickUsingJavaScript(By locator) {
        executeScript("arguments[0].click();", waitForPresence(locator));
    }

    // FILE UPLOADS / SCREENSHOTS
    /** Target an input[type=file]; does not automate the operating-system picker.
     * Remote Grid uploads additionally require a LocalFileDetector on RemoteWebDriver.
     */
    public void uploadFile(By fileInput, Path file) {
        Path absolute = file.toAbsolutePath().normalize();
        if (!Files.isRegularFile(absolute)) {
            throw new IllegalArgumentException("Upload file not found: " + absolute);
        }
        waitForPresence(fileInput).sendKeys(absolute.toString());
    }

    /** PNG bytes suitable for Cucumber scenario.attach(bytes, "image/png", "failure"). */
    public byte[] screenshotBytes() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /** Creates directories and a unique PNG filename, avoiding parallel-run collisions. */
    public Path saveScreenshot(Path directory, String name) throws IOException {
        Path absolute = directory.toAbsolutePath().normalize();
        Files.createDirectories(absolute);
        String safeName = Objects.requireNonNull(name, "name")
            .replaceAll("[^a-zA-Z0-9_-]", "_");
        Path output = Files.createTempFile(absolute, "shot-" + safeName + "-", ".png");
        Files.write(output, screenshotBytes());
        return output;
    }

    // COOKIES: first navigate to the cookie's domain.
    public void addCookie(Cookie cookie) { driver.manage().addCookie(cookie); }
    public Cookie getCookie(String name) { return driver.manage().getCookieNamed(name); }
    public void deleteCookie(String name) { driver.manage().deleteCookieNamed(name); }
    public void deleteAllCookies() { driver.manage().deleteAllCookies(); }
}
