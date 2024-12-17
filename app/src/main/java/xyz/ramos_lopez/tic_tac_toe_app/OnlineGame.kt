data class OnlineGame(
    var gameId: String = "",
    var player1: String = "",
    var player2: String? = null,
    var board: List<String> = List(9) { "" },
    var currentTurn: String = "",
    var winner: String? = null,
    var status: String = "waiting" // waiting, active, finished
)