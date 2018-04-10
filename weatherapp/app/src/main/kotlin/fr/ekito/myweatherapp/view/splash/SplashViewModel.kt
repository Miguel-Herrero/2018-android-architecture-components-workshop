package fr.ekito.myweatherapp.view.splash

import android.arch.lifecycle.LiveData
import fr.ekito.myweatherapp.data.repository.WeatherRepository
import fr.ekito.myweatherapp.util.mvvm.RxViewModel
import fr.ekito.myweatherapp.util.mvvm.SingleLiveEvent
import fr.ekito.myweatherapp.util.rx.SchedulerProvider
import fr.ekito.myweatherapp.util.rx.with
import fr.ekito.myweatherapp.view.Event
import fr.ekito.myweatherapp.view.FailedEvent
import fr.ekito.myweatherapp.view.LoadingEvent
import fr.ekito.myweatherapp.view.SuccessEvent

class SplashViewModel(
    private val weatherRepository: WeatherRepository,
    private val schedulerProvider: SchedulerProvider
) : RxViewModel() {

    /*
     * We use SingleLiveEvent to publish "events"
     * No need to publish and retain any view state
     */
    private val mEvents = SingleLiveEvent<Event>()
    val events: LiveData<Event>
        get() = mEvents

    fun getLastWeather() {
        mEvents.value = LoadingEvent
        launch {
            weatherRepository.getWeather().with(schedulerProvider)
                .toCompletable()
                .subscribe({ mEvents.value = SuccessEvent },
                    { error -> mEvents.value = FailedEvent(error) })
        }
    }
}