import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import xyz.ramos_lopez.tic_tac_toe_app.GameLogic
import xyz.ramos_lopez.tic_tac_toe_app.TicTacToeGame


class OnlineTicTacToeGame(
    private val gameId: String,
    private val currentPlayer: String,
    private val database: FirebaseDatabase
) : GameLogic {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }
    private var mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val gameRef = database.getReference("partidas_activas").child(gameId)

    init {
        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(OnlineGame::class.java)
                game?.let { updateGameState(it) }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlineTicTacToeGame", "Error: ${error.message}")
            }
        })
    }

    private fun updateGameState(game: OnlineGame) {
        game.board.forEachIndexed { index, value ->
            mBoard[index] = when(value) {
                "X" -> HUMAN_PLAYER
                "O" -> COMPUTER_PLAYER
                else -> OPEN_SPOT
            }
        }
    }


    override fun setMove(player: Char, location: Int) {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            val updates = hashMapOf<String, Any>(
                "board/${location}" to player.toString(),
                "currentTurn" to if(currentPlayer == player.toString()) "player2" else "player1"
            )
            gameRef.updateChildren(updates)
        }
    }

    override fun clearBoard() {
        val emptyBoard = List(BOARD_SIZE) { "" }
        gameRef.child("board").setValue(emptyBoard)
    }

    override fun getBoardState(): CharArray = mBoard.clone()

    override fun setBoardState(board: CharArray) {
        mBoard = board.clone()
        val updates = board.mapIndexed { index, value ->
            "board/$index" to value.toString()
        }.toMap()
        gameRef.updateChildren(updates)
    }

    override fun getBoardValue(position: Int): Char {
        return if (position in 0 until BOARD_SIZE) mBoard[position] else OPEN_SPOT
    }

    override fun checkForWinner(): Int {
        // Check for winner
        // Return: 0 for no winner, 1 for tie, 2 for HUMAN_PLAYER, 3 for COMPUTER_PLAYER
        
        // Check rows
        for (i in 0..6 step 3) {
            if (mBoard[i] != OPEN_SPOT && 
                mBoard[i] == mBoard[i + 1] && 
                mBoard[i] == mBoard[i + 2]) {
                return if (mBoard[i] == HUMAN_PLAYER) 2 else 3
            }
        }

        // Check columns
        for (i in 0..2) {
            if (mBoard[i] != OPEN_SPOT && 
                mBoard[i] == mBoard[i + 3] && 
                mBoard[i] == mBoard[i + 6]) {
                return if (mBoard[i] == HUMAN_PLAYER) 2 else 3
            }
        }

        // Check diagonals
        if (mBoard[0] != OPEN_SPOT && 
            mBoard[0] == mBoard[4] && 
            mBoard[0] == mBoard[8]) {
            return if (mBoard[0] == HUMAN_PLAYER) 2 else 3
        }
        
        if (mBoard[2] != OPEN_SPOT && 
            mBoard[2] == mBoard[4] && 
            mBoard[2] == mBoard[6]) {
            return if (mBoard[2] == HUMAN_PLAYER) 2 else 3
        }

        // Check for tie
        if (!mBoard.contains(OPEN_SPOT)) {
            return 1
        }

        return 0 // No winner yet
    }

    // These methods are not used in online game but required by interface
    override fun getDifficultyLevel(): TicTacToeGame.DifficultyLevel = 
        TicTacToeGame.DifficultyLevel.Expert

    override fun setDifficultyLevel(difficultyLevel: TicTacToeGame.DifficultyLevel) {
        // Not used in online game
    }

    override fun getComputerMove(): Int = -1 // Not used in online game
}