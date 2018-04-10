package fr.ekito.myweatherapp.view.weather

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import fr.ekito.myweatherapp.data.repository.WeatherRepository
import fr.ekito.myweatherapp.domain.DailyForecastModel
import fr.ekito.myweatherapp.util.mvvm.RxViewModel
import fr.ekito.myweatherapp.util.mvvm.SingleLiveEvent
import fr.ekito.myweatherapp.util.rx.SchedulerProvider
import fr.ekito.myweatherapp.util.rx.with
import fr.ekito.myweatherapp.view.ErrorState
import fr.ekito.myweatherapp.view.Event
import fr.ekito.myweatherapp.view.LoadingState
import fr.ekito.myweatherapp.view.State

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val schedulerProvider: SchedulerProvider
) : RxViewModel() {

    private val mStates = MutableLiveData<State>()
    val states: LiveData<State>
        get() = mStates

    private val mEvents = SingleLiveEvent<Event>()
    val events: LiveData<Event>
        get() = mEvents

    /**
     * Load new weather for given location
     * notify for loading: LoadingLocationEvent / LoadLocationFailedEvent
     * push WeatherListState if it succeed
     */
    fun loadNewLocation(newLocation: String) {
        mEvents.value = LoadingLocationEvent(newLocation)
        launch {
            weatherRepository.getWeather(newLocation)
                .with(schedulerProvider)
                .subscribe(
                    { weather -> mStates.value = WeatherListState.from(weather) },
                    { error -> mEvents.value = LoadLocationFailedEvent(newLocation, error) })
        }
    }

    /**
     * Retrieve previously loaded weather and push view states
     */
    fun getWeather() {
        mStates.value = LoadingState
        launch {
            weatherRepository.getWeather()
                .with(schedulerProvider)
                .subscribe(
                    { weather -> mStates.value = WeatherListState.from(weather) },
                    { error -> mStates.value = ErrorState(error) })
        }
    }

    data class WeatherListState(
        val location: String,
        val first: DailyForecastModel,
        val lasts: List<DailyForecastModel>
    ) : State() {
        companion object {
            fun from(list: List<DailyForecastModel>): WeatherListState {
                return if (list.isEmpty()) error("weather list should not be empty")
                else {
                    val first = list.first()
                    val location = first.location
                    WeatherListState(location, first, list.takeLast(list.size - 1))
                }
            }
        }
    }

    data class LoadingLocationEvent(val location: String) : Event()
    data class LoadLocationFailedEvent(val location: String, val error: Throwable) : Event()
}