package org.futo.circles.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.futo.circles.R
import org.futo.circles.databinding.ViewPollContentBinding
import org.futo.circles.core.model.PollContent
import org.futo.circles.core.model.PollState

class PollContentView(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    private val binding =
        ViewPollContentBinding.inflate(LayoutInflater.from(context), this)


    fun setup(poll: PollContent, onOptionSelected: (String) -> Unit) {
        with(binding) {
            tvPollQuestions.text = poll.question
            lvOptionsContainer.removeAllViews()
            poll.options.forEach {
                val layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, 16, 0, 0)
                lvOptionsContainer.addView(
                    PollOptionView(context).apply {
                        setup(it, poll.isClosedType, poll.state, onOptionSelected)
                    },
                    layoutParams
                )
            }
            tvPollStatus.text =
                createPollStatusMessage(poll.isClosedType, poll.state, poll.totalVotes)
        }
    }

    private fun createPollStatusMessage(
        isClosedType: Boolean,
        pollState: PollState,
        votes: Int
    ): String = when {
        pollState == PollState.Ended -> context.resources.getQuantityString(
            R.plurals.poll_total_vote_count_after_ended, votes, votes
        )
        isClosedType -> context.getString(R.string.poll_closed_not_voted)
        pollState == PollState.Voted -> context.resources.getQuantityString(
            R.plurals.poll_total_vote_count_before_ended_and_voted, votes, votes
        )
        votes == 0 -> context.getString(R.string.poll_no_votes_cast)
        else -> context.resources.getQuantityString(
            R.plurals.poll_total_vote_count_before_ended_and_not_voted, votes, votes
        )
    }
}