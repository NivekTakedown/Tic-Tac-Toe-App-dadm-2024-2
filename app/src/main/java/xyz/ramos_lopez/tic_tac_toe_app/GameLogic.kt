package xyz.ramos_lopez.tic_tac_toe_app

interface GameLogic {
    fun getDifficultyLevel(): TicTacToeGame.DifficultyLevel
    fun setDifficultyLevel(difficultyLevel: TicTacToeGame.DifficultyLevel)
    fun clearBoard()
    fun setMove(player: Char, location: Int)
    fun getComputerMove(): Int
    fun checkForWinner(): Int
    fun getBoardState(): CharArray
    fun setBoardState(board: CharArray)
    fun getBoardValue(position: Int): Char
    fun restartGame()
}