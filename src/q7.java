public class q7 {

    //same varibles from the FSP
    private enum Turn {SHARKS, JETS}
    private boolean sharksHandkerchief = false;
    private boolean jetsHandkerchief = false;
    private Turn turn = Turn.SHARKS; //arbitrary sharks go first same as FSP

    //both gangs tie handkerchief when they come to the court or else they wait
    public synchronized void tieHandkerchief(String gang) throws InterruptedException {
        try {
            while ((gang.equals("sharks") && sharksHandkerchief) || (gang.equals("jets") && jetsHandkerchief)) {
                wait(); //wait until a handkerchief is untied, cant tie if already tied
            }
            // as per FSP both gangs initially tie their handkerchiefs if they want to enter, and they set the turn to the OTHER gang
            if (gang.equals("sharks")) {
                sharksHandkerchief = true; //sharks.tieRed
                turn = Turn.JETS; //sharks.setTurn
                System.out.println("sharks have tied their handkerchief, setting the turn to jets.");
            } else if (gang.equals("jets")) {
                jetsHandkerchief = true; //jets.tieRed
                turn = Turn.SHARKS; //jets.setTurn
                System.out.println("jets have tied their handkerchief, setting the turn to sharks");
            }
            displayState();
        } catch (InterruptedException e) {
            System.out.println(gang + " was interrupted while tying the red handkerchief.");
            //bail out of the transaction and reset the state if there was an exception
            if (gang.equals("sharks")) {
                sharksHandkerchief = false;
            } else if (gang.equals("jets")) {
                jetsHandkerchief = false;
            }
            throw e;
        } finally {
            notifyAll();
            displayState();
        }
    }

    //sharks have to wait if the jets handkerchief is up AND its the jets turn, if its not the jets turn, or it is their turn but they dont want to enter (their handkerchief is down) then the sharks can enter the court. as per FSP
    public synchronized void checkAndEnter(String gang) throws InterruptedException {
        try{
            //checkTurn action
            while ((gang.equals("sharks") && jetsHandkerchief && turn == Turn.JETS ) || (gang.equals("jets") && sharksHandkerchief && turn == Turn.SHARKS)) {
                System.out.println(gang + " is  waiting to enter the court..");
                // wait action
                wait(); //wait until its our turn or the other gang has their handkerchief untied
            }
            //entering the court, just printlining because theres no data to be changed in the court itself
            if (gang.equals("sharks")) {
                System.out.println("sharks enter the court and will now play"); //sharks.arrive -> sharks.play
            } else if (gang.equals("jets")) {
                System.out.println("jets enter the court and will now play");
            }
            displayState();
        } catch (InterruptedException e) {
            System.out.println(gang + " was interrupted while waiting or entering the court.");
            //if there was a state change id do it here like reverting some changed state or something like that
            throw e;
        } finally {
            notifyAll();
        }
    }

    // as per FSP gang has to leave after it has arrive -> play. im action hiding the play because its not important here
    public synchronized void leaveCourt(String gang) {
        if (gang.equals("sharks")) {
            sharksHandkerchief = false; //sharks.leave -> sharks.untieRed
            System.out.println("sharks leave the court and untie their red handkerchief.");
        } else if (gang.equals("jets")) {
            jetsHandkerchief = false; //jets.leave -> jets.untieRed
            System.out.println("jets leave the court and untie their red handkerchief.");
        }
        notifyAll(); //notify both gangs if theres a change in handkerchief tying
        displayState();
    }


    private synchronized void displayState() {
        System.out.println("current state: ");
        System.out.println("  sharks Handkerchief: " + sharksHandkerchief);
        System.out.println("  jets Handkerchief: " + jetsHandkerchief);
        System.out.println("  turn: " + turn);
        System.out.println("------------------------------------");
    }

    public static void main(String[] args) {
        q7 court = new q7();

        Runnable sharkRunnable = () -> {
            try {
                while (true) {
                    court.tieHandkerchief("sharks");
                    court.checkAndEnter("sharks");
                    Thread.sleep(1000); //sharks.play
                    court.leaveCourt("sharks");
                    Thread.sleep(1000); //waiting
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable jetsRunnable = () -> {
            try {
                while (true) {
                    court.tieHandkerchief("jets");
                    court.checkAndEnter("jets");
                    Thread.sleep(1000); //jets.play
                    court.leaveCourt("jets");
                    Thread.sleep(1000); //waiting
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread sharksThread = new Thread(sharkRunnable);
        Thread jetsThread = new Thread(jetsRunnable);
        sharksThread.start();
        jetsThread.start();
    }
}
