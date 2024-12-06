package xyz.ramos_lopez.tic_tac_toe_app

import kotlin.random.Random

class TicTacToeGame {
    enum class DifficultyLevel {
        Easy, Harder, Expert
    }

    private var mDifficultyLevel = DifficultyLevel.Expert
    private var mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val mRand = Random

    fun getDifficultyLevel(): DifficultyLevel = mDifficultyLevel
    fun setDifficultyLevel(difficultyLevel: DifficultyLevel) {
        mDifficultyLevel = difficultyLevel
    }

    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }

    /** Limpia el tablero de todas las X y O estableciendo todas las posiciones a OPEN_SPOT. */
    fun clearBoard() {
        for (i in 0 until BOARD_SIZE) {
            mBoard[i] = OPEN_SPOT
        }
    }

    /**
     * Establece el jugador dado en la ubicación dada en el tablero del juego.
     * La ubicación debe estar disponible, o el tablero no cambiará.
     */
    fun setMove(player: Char, location: Int) {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player
        }
    }

    /**
     * Devuelve el mejor movimiento para que haga la computadora. Debes llamar a setMove()
     * para que la computadora se mueva realmente a esa ubicación.
     * @return El mejor movimiento para que la computadora haga (0-8).
     */
    fun getComputerMove(): Int {
        return when (mDifficultyLevel) {
            DifficultyLevel.Easy -> getRandomMove()
            DifficultyLevel.Harder -> {
                getWinningMove() ?: getRandomMove()
            }
            DifficultyLevel.Expert -> {
                getWinningMove() ?: getBlockingMove() ?: getRandomMove()
            }
        }
    }

    private fun getRandomMove(): Int {
        var move: Int
        do {
            move = mRand.nextInt(BOARD_SIZE)
        } while (mBoard[move] != OPEN_SPOT)
        return move
    }

    private fun getWinningMove(): Int? {
        // Buscar movimiento ganador
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    mBoard[i] = OPEN_SPOT
                    return i
                }
                mBoard[i] = OPEN_SPOT
            }
        }
        return null
    }

    private fun getBlockingMove(): Int? {
        // Buscar movimiento para bloquear al jugador
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    mBoard[i] = OPEN_SPOT
                    return i
                }
                mBoard[i] = OPEN_SPOT
            }
        }
        return null
    }

    /**
     * Verifica si hay un ganador y devuelve un valor de estado que indica quién ha ganado.
     * @return Devuelve 0 si no hay ganador o empate aún, 1 si es un empate, 2 si X ganó,
     * o 3 si O ganó.
     */
    fun checkForWinner(): Int {
        // Verificar victorias horizontales
        for (i in 0..6 step 3) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 1] == HUMAN_PLAYER && mBoard[i + 2] == HUMAN_PLAYER)
                return 2
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i + 1] == COMPUTER_PLAYER && mBoard[i + 2] == COMPUTER_PLAYER)
                return 3
        }

        // Verificar victorias verticales
        for (i in 0..2) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 3] == HUMAN_PLAYER && mBoard[i + 6] == HUMAN_PLAYER)
                return 2
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i + 3] == COMPUTER_PLAYER && mBoard[i + 6] == COMPUTER_PLAYER)
                return 3
        }

        // Verificar victorias diagonales
        if ((mBoard[0] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[8] == HUMAN_PLAYER) ||
            (mBoard[2] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[6] == HUMAN_PLAYER)
        )
            return 2
        if ((mBoard[0] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[8] == COMPUTER_PLAYER) ||
            (mBoard[2] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[6] == COMPUTER_PLAYER)
        )
            return 3

        // Verificar empate
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT)
                return 0
        }

        // Si llegamos aquí, es empate
        return 1
    }

    fun getBoardState(): CharArray {
        return mBoard.clone()
    }

    fun setBoardState(board: CharArray) {
        mBoard = board.clone()
    }

    fun getBoardValue(position: Int): Char = mBoard[position]
}
