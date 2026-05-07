// IObserver — інтерфейс спостерігача
public interface IObserver
{
    void Update(float temperature, float humidity, float pressure);
}

// ISubject — інтерфейс суб'єкта
public interface ISubject
{
    void Attach(IObserver observer);
    void Detach(IObserver observer);
    void Notify();
}

// ConcreteSubject — метеостанція
public class WeatherStation : ISubject
{
    private readonly List<IObserver> _observers = new();
    private float _temperature;
    private float _humidity;
    private float _pressure;

    public void Attach(IObserver observer) => _observers.Add(observer);
    public void Detach(IObserver observer) => _observers.Remove(observer);

    public void Notify()
    {
        foreach (var observer in _observers)
            observer.Update(_temperature, _humidity, _pressure);
    }

    public void SetMeasurements(float temperature, float humidity,
                                 float pressure)
    {
        _temperature = temperature;
        _humidity    = humidity;
        _pressure    = pressure;
        Notify();
    }
}

// ConcreteObserver A — мобільний застосунок
public class MobileApp : IObserver
{
    public void Update(float temperature, float humidity, float pressure)
    {
        Console.WriteLine($"[Мобільний застосунок] " +
            $"Температура: {temperature}°C, Вологість: {humidity}%, " +
            $"Тиск: {pressure} гПа");
    }
}

// ConcreteObserver B — веб-панель
public class WebDashboard : IObserver
{
    public void Update(float temperature, float humidity, float pressure)
    {
        Console.WriteLine($"[Веб-панель] " +
            $"Оновлено: {temperature}°C / {humidity}% / {pressure} гПа");
    }
}

// ConcreteObserver C — система автоматизації будинку
public class HomeAutomation : IObserver
{
    public void Update(float temperature, float humidity, float pressure)
    {
        if (temperature > 30)
            Console.WriteLine("[Автоматизація] Увімкнено кондиціонер.");
        else
            Console.WriteLine("[Автоматизація] Кондиціонер не потрібен.");
    }
}

// Client — клієнтський код
class Program
{
    static void Main()
    {
        var station    = new WeatherStation();
        var mobile     = new MobileApp();
        var dashboard  = new WebDashboard();
        var automation = new HomeAutomation();

        station.Attach(mobile);
        station.Attach(dashboard);
        station.Attach(automation);

        station.SetMeasurements(25.0f, 60.0f, 1013.0f);
        // [Мобільний застосунок] Температура: 25°C, Вологість: 60%, Тиск: 1013 гПа
        // [Веб-панель] Оновлено: 25°C / 60% / 1013 гПа
        // [Автоматизація] Кондиціонер не потрібен.

        station.Detach(dashboard); // скасування реєстрації веб-панелі

        station.SetMeasurements(32.5f, 55.0f, 1010.0f);
        // [Мобільний застосунок] Температура: 32.5°C, Вологість: 55%, Тиск: 1010 гПа
        // [Автоматизація] Увімкнено кондиціонер.
    }
}
