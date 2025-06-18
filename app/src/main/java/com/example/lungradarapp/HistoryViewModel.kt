import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lungradarapp.AnalysisResult
import com.example.lungradarapp.LungCancerDatabaseHelper
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = LungCancerDatabaseHelper(application)

    private val _allResults = MutableLiveData<List<AnalysisResult>>()
    val allResults: LiveData<List<AnalysisResult>> get() = _allResults

    // Method to load results asynchronously
    fun loadResults() {
        viewModelScope.launch {
            val results = databaseHelper.getAllResults()
            _allResults.value = results
        }
    }
}
