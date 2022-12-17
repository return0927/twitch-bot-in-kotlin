package kr.enak.luya.luyasupport.twitch.abc

abstract class AbstractPrefixCommand(
    open val name: String,
    open val aliases: Array<out String>,
): ICommand {
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPrefixCommand) return false

        if (name != other.name) return false
        if (!aliases.contentEquals(other.aliases)) return false

        return true
    }
}
