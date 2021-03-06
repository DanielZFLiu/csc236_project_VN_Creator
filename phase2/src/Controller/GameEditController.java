package Controller;
import Interface.UserData;
import Presenter.GamePresenter;
import UseCase.GamesUseCase;
import UseCase.GameUseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class to edit games.
 *
 * @author Daniel Liu
 */

public class GameEditController {

    private final UserData userData;
    private final GamesUseCase gamesUseCase;
    private final GameUseCase gameUseCase = new GameUseCase();
    private final GamePresenter gamePresenter;

    /**
     * Contructor for the class.
     *
     * @param gamesUseCase A <GamesUseCase> containing current game data.
     * @param userData A UserData interface containing info on current existing users.
     */

    public GameEditController(GamesUseCase gamesUseCase, UserData userData, GamePresenter gamePresenter){
        this.gamesUseCase = gamesUseCase;
        this.userData = userData;
        this.gamePresenter = gamePresenter;
    }

    /**
     * Method to edit existing games. Interacts with GamesUseCase and allows users to make changes to existing games.
     */

    public void editGame(){
        presentAvailableGames();

        List<String> tags = new ArrayList<>();
        tags.add("Game Name");
        String gameName = gamePresenter.displayInputs(this, tags,
                "Enter the name of the game you want to play.").get(0);
        if(!verifyEditGameRight(gameName)){ return; }

        List<Object> initialIdAndDialogue = gameUseCase.openGame(gamesUseCase, gameName);
        List<String> choices = new ArrayList<>();
        choices.add("Change Game State");
        choices.add("Edit Game Dialogues");
        choices.add("Exit and Save");
        if (userData.currentUser().startsWith("Admin_")){
            choices.add("Delete Game");
        }

        editGameMenu(gameName, initialIdAndDialogue, choices);
    }

    private void editGameMenu(String gameName, List<Object> initialIdAndDialogue, List<String> choices) {
        int userChoice;
        while (true){
            userChoice = gamePresenter.displayChoices(this, choices, "");

            if(userChoice == 0){
                if (changeGameState(gameName)) break;
            }
            else if(userChoice == 1){
                editGameDialogues(gameName, (int) initialIdAndDialogue.get(0));
            }
            else if(userChoice == 2){
                gamesUseCase.saveGames();
                break;
            }
            else if(userChoice == 3){
                gamesUseCase.deleteGame(gameName);
                gamesUseCase.saveGames();
                break;
            }
        }
    }

    private boolean changeGameState(String gameName) {
        gamesUseCase.changeGameState(gameName);
        if(gamesUseCase.getPrivateGames().contains(gameName)){
            gamePresenter.displayTextScene(this, "Game state changed to Private.");
        }
        else{
            gamePresenter.displayTextScene(this, "Game state changed to Public.");
        }
        if (!userData.currentUser().startsWith("Admin_")){
            gamesUseCase.saveGames();
            return true;
        }
        return false;
    }

    private void presentAvailableGames() {
        List<String> newGames = new ArrayList<>();
        if (userData.currentUser().startsWith("Admin_")){
            newGames.addAll(gamesUseCase.getPrivateGames());
            newGames.addAll(gamesUseCase.getPublicGames());
        }
        else{
            newGames.addAll(gamesUseCase.getPrivateGames(userData.currentUser()));
            newGames.addAll(gamesUseCase.getPublicGamesByAuthor(userData.currentUser()));
        }
        gamePresenter.displayList(this, newGames, "This is the list of available games.");
    }

    private boolean verifyEditGameRight(String gameName){
        boolean privateGameByUser;
        boolean publicGameByUser;
        if (userData.currentUser().startsWith("Admin_")){
            privateGameByUser = gamesUseCase.getPrivateGames().contains(gameName);
            publicGameByUser = gamesUseCase.getPublicGames().contains(gameName);
        }
        else{
            privateGameByUser = gamesUseCase.getPrivateGames(userData.currentUser()).contains(gameName);
            publicGameByUser = gamesUseCase.getPublicGamesByAuthor(userData.currentUser()).contains(gameName);
        }

        if(!privateGameByUser && publicGameByUser && !userData.currentUser().startsWith("Admin_")){
            gamePresenter.displayTextScene(this, "Changing game to private to edit...");
            gamesUseCase.changeGameState(gameName);
        }

        else if(!privateGameByUser && !publicGameByUser){
            gamePresenter.displayTextScene(this, "You cannot edit this game!");
            return false;
        }

        return true;
    }

    private void editGameDialogues(String gameName, int currentId){
        while (true) {
            gamePresenter.displayTextScene(this, "Dialogue ID " + currentId + ": " +
                            gameUseCase.getDialogueById(currentId),
                    gamesUseCase.getGameAsString(gameName, 150, currentId));

            String editGameDialogueChoices = "Enter r to return to the parent dialogue, " +
                    "v + id to view the dialogue with that id (e.g. v1), " +
                    "c + id to change a game dialogue, " +
                    "a + id to add a dialogue, d + id to delete a dialogue, " +
                    "and e to exit.";
            String userChoice = gamePresenter.displayTextSceneInput(this, editGameDialogueChoices,
                    gamesUseCase.getGameAsString(gameName, 150, currentId));

            ArrayList<Object> actionAndId = this.EGDVerifyUserChoice(userChoice);
            if (actionAndId.size() == 0){
                gamePresenter.displayTextScene(this, "Wrong input.");
                continue;
            }
            char action = (char) actionAndId.get(0);
            int id = 0;
            if(actionAndId.size() == 2){
                id = (int) actionAndId.get(1);
            }

            if (editGameDialoguesActionHelper(gameName, currentId, action, id)) return;
        }
    }

    private boolean editGameDialoguesActionHelper(String gameName, int currentId, char action, int id) {
        switch (action){
            case 'r':
                int parentId = gameUseCase.getParentDialogueId(currentId);
                if (parentId != -1){
                    this.editGameDialogues(gameName, parentId);
                    return true;
                }
                break;
            case 'v':
                this.editGameDialogues(gameName, id);
                return true;
            case 'c':
                this.changeDialogue(gameName, id, currentId);
                break;
            case 'a':
                this.addDialogue(id);
                break;
            case 'd':
                if (!gameUseCase.deleteDialogueById(id)){
                    gamePresenter.displayTextScene(this, "You cannot delete the first dialogue of the game!");
                }
                break;
            case 'e':
                return true;
        }
        return false;
    }

    private ArrayList<Object> EGDVerifyUserChoice(String userChoice){
        ArrayList<Object> actionAndId = new ArrayList<>();
        if (userChoice.equals("")){
            return new ArrayList<>();
        }
        Character action = userChoice.charAt(0);
        if (action.equals('r') || action.equals('e')) {
            if (userChoice.length() == 1){
                actionAndId.add(action);
                return actionAndId;
            }
            return actionAndId;
        }

        int id;
        try {
            id = Integer.parseInt(userChoice.substring(1));
        } catch (NumberFormatException b) {
            return actionAndId;
        }

        boolean rightAction = action == 'v' ||  action == 'c' || action == 'a' ||  action == 'd';
        boolean rightId = gameUseCase.isIdInGame(id);

        if ((!rightAction || !rightId)){
            return actionAndId;
        }

        actionAndId.add(action);
        actionAndId.add(id);
        return actionAndId;
    }

    private void changeDialogue(String gameName, int id, int currentId){
        String newDialogue = gamePresenter.displayTextSceneInput(this, "Enter the new dialogue: ",
                gamesUseCase.getGameAsString(gameName, 150, currentId));
        gameUseCase.setDialogueById(id, newDialogue);
    }

    private void addDialogue(int id){
        List<String> tags = new ArrayList<>();
        tags.add("Description:");
        tags.add("Dialogue:");
        List<String> inputs = gamePresenter.displayInputs(this, tags,
                "Enter the new choice you want to add to the dialogue with id " + id + ".");
        if(inputs.get(0).equals("")){
            inputs.set(0, " ");
        }
        gameUseCase.addChoiceToDialogue(inputs.get(1), inputs.get(0), id);
    }
}
