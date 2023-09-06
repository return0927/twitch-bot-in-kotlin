package kr.enak.luya.luyasupport.twitch.abc

interface ICommand {
    suspend fun execute(dto: IncomingCommandDto): String?
}