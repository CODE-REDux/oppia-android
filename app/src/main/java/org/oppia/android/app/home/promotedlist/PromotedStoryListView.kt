package org.oppia.android.app.home.promotedlist

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.shim.ViewBindingShim
import org.oppia.android.app.shim.ViewComponentFactory
import javax.inject.Inject

/**
 * A custom [RecyclerView] for displaying a variable list of promoted lesson stories that snaps to
 * a fixed position when being scrolled.
 */
class PromotedStoryListView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
  private val TAG = "PromotedStoryListView"

  @Inject
  lateinit var bindingInterface: ViewBindingShim

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    (FragmentManager.findFragment<Fragment>(this) as ViewComponentFactory)
      .createViewComponent(this).inject(this)

    // The StartSnapHelper is used to snap between items rather than smooth scrolling, so that
    // the item is completely visible in [HomeFragment] as soon as learner lifts the finger
    // after scrolling.
    val snapHelper = StartSnapHelper()
    onFlingListener = null
    snapHelper.attachToRecyclerView(this)
  }

  /**
   * Sets the list of promoted stories that this view shows to the learner.
   *
   * @param newDataList the new list of stories to present
   */
  fun setPromotedStoryList(@Nullable newDataList: List<PromotedStoryViewModel>) {
    // To reliably bind data only after the adapter is created, we manually set the data so we can first
    // check for the adapter; when using an existing [RecyclerViewBindingAdapter] there is no reliable
    // way to check that the adapter is created.
    // This ensures that the adapter will only be created once and correctly rebinds the data.
    // For more context:  https://github.com/oppia/oppia-android/pull/2246#pullrequestreview-565964462
    if (adapter == null) {
      adapter = createAdapter()
    }

    if (newDataList == null || newDataList.isEmpty()) {
      Log.w(TAG, ": failed to resolve new story list data")
    } else {
      // Only re-bind and display the data if it's a valid list of promoted items for learners
      (adapter as BindableAdapter<PromotedStoryViewModel>).setDataUnchecked(newDataList)
    }
  }

  private fun createAdapter(): BindableAdapter<PromotedStoryViewModel> {
    return BindableAdapter.SingleTypeBuilder.newBuilder<PromotedStoryViewModel>()
      .registerViewBinder(
        inflateView = { parent ->
          bindingInterface.providePromotedStoryCardInflatedView(
            LayoutInflater.from(parent.context),
            parent,
            attachToParent = false
          )
        },
        bindView = { view, viewModel ->
          bindingInterface.providePromotedStoryViewModel(
            view,
            viewModel
          )
        }
      ).build()
  }
}