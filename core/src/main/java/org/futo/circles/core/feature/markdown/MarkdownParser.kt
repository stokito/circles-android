package org.futo.circles.core.feature.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.Emphasis
import org.commonmark.node.StrongEmphasis
import org.futo.circles.core.extensions.notEmptyDisplayName
import org.futo.circles.core.feature.markdown.mentions.plugin.MentionPlugin
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getUserOrDefault


object MarkdownParser {

    private var markwonBulder: Markwon? = null

    fun parse(text: String): Spanned = markwonBulder?.toMarkdown(text) ?: SpannableString(text)

    fun clearInstance() {
        markwonBulder = null
    }

    //require UI context for Mentions span
    fun initBuilder(context: Context) = markwonBuilder(context).also { markwonBulder = it }

    private fun markwonBuilder(context: Context): Markwon = Markwon.builder(context)
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(MentionPlugin(context))
        .build()

    fun markwonNotificationBuilder(context: Context): Markwon = Markwon.builder(context)
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                builder.setFactory(
                    Emphasis::class.java
                ) { _, _ -> StyleSpan(Typeface.ITALIC) }
            }
        })
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                builder.setFactory(
                    StrongEmphasis::class.java
                ) { _, _ -> StyleSpan(Typeface.BOLD) }
            }
        })
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .build()

    fun hasCurrentUserMention(text: String): Boolean {
        val session = MatrixSessionProvider.currentSession ?: return false
        val userName = session.getUserOrDefault(session.myUserId).notEmptyDisplayName()
        val mentionString = "@$userName@"
        return text.contains(mentionString)
    }

}