package cpen221.mp2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DWInteractionGraph {
    private static final int SENDER = 0;
    private static final int RECEIVER = 1;
    private static final int TIME = 2;
    private static final int WEIGHT = 2;

    private HashMap<Integer, LinkedList<Edge>> DWG;
    private List<List<Integer>> emailData;
    private List<List<Integer>> emailDataWithWeight;
    private Set<Integer> userSet;
    private List<Integer> userList;
    /* Representation Invariant */
    // TODO: Write RI

    /* Abstraction Function */
    //TODO: write AF


    /*Safety from rep exposure:*/
    // All fields are private
    // Use defensive copying when returning a mutable object

    /**
     * Creates a new DWInteractionGraph using an email interaction file.
     * The email interaction file will be in the resource directory.
     *
     * @param fileName the name of the file in the resource
     *                 directory containing email interactions
     */
    public DWInteractionGraph(String fileName) {
        emailData = new ArrayList<>(processData(fileName));
        setEmailDataWithWeight(emailData);

        makeDWI();

        // remove below
        for (List l : emailData) {
            System.out.println(l);
        }
        System.out.println("User Set" + userSet);
        System.out.println("User List is " + userList);
        System.out.println("===Email data with weight===");
        for (List<Integer> ll : emailDataWithWeight) {
            System.out.println(ll);
        }
        printGraph();
    }

    // Make DWI graph
    private void makeDWI() {
        DWG = new HashMap<Integer, LinkedList<Edge>>();

        for (Integer sender : userList) {
            LinkedList<Edge> tempList = new LinkedList<>();
            for (List data : emailDataWithWeight) {
                if (data.get(SENDER) == sender) {
                    // To prevent rep exposure. Not sure that this will be needed. But just in case
                    int receiver = (int) data.get(RECEIVER);
                    int weight = (int) data.get(WEIGHT);
                    tempList.add(new Edge((int) sender, receiver, weight));
                }

            }
            DWG.put(sender, tempList);
        }

    }

    /**
     * Creates a new DWInteractionGraph from a DWInteractionGraph object
     * and considering a time window filter.
     *
     * @param inputDWIG  a DWInteractionGraph object
     * @param timeFilter an integer array of length 2: [t0, t1]
     *                   where t0 <= t1. The created DWInteractionGraph
     *                   should only include those emails in the input
     *                   DWInteractionGraph with send time t in the
     *                   t0 <= t <= t1 range.
     */
    public DWInteractionGraph(DWInteractionGraph inputDWIG, int[] timeFilter) {
        int START_TIME_INDEX = 0;
        int END_TIME_INDEX = 1;
        List<List<Integer>> dataOfInput = new ArrayList<>(inputDWIG.getDWI_data());
        List<List<Integer>> timeFilteredData = new ArrayList<>();
        for (List l : dataOfInput) {
            if ((int) l.get(TIME) >= timeFilter[START_TIME_INDEX] &&
                (int) l.get(TIME) <= timeFilter[END_TIME_INDEX]) {
                timeFilteredData.add(l);
            }
        }

        emailData = new ArrayList<>(timeFilteredData);
        setEmailDataWithWeight(timeFilteredData);

        makeDWI();

        // Remove
        System.out.println();
        System.out.println("===Time Filtered DWI===");
        printGraph();
    }


    /**
     * Creates a new DWInteractionGraph from a DWInteractionGraph object
     * and considering a list of User IDs.
     *
     * @param inputDWIG  a DWInteractionGraph object
     * @param userFilter a List of User IDs. The created DWInteractionGraph
     *                   should exclude those emails in the input
     *                   DWInteractionGraph for which neither the sender
     *                   nor the receiver exist in userFilter.
     */
    public DWInteractionGraph(DWInteractionGraph inputDWIG, List<Integer> userFilter) {
        List<List<Integer>> inputData = new ArrayList<>(inputDWIG.getDWI_data());
        Set<List<Integer>> userFilteredSet = new HashSet<>();
        List<List<Integer>> userFilteredData;

        for (Integer user : userFilter) {
            for (List list : inputData) {
                if (list.get(SENDER) == user || list.get(RECEIVER) == user) {
                    userFilteredSet.add(list);
                }
            }
        }

        userFilteredData = new ArrayList<>(userFilteredSet);
        emailData = new ArrayList<>(userFilteredData);
        setEmailDataWithWeight(userFilteredData);

        // remove
        System.out.println("email data with weight after user filter: ");
        for (List l : emailDataWithWeight) {
            System.out.println(l);
        }
        makeDWI();

        // Remove
        System.out.println();
        System.out.println("===User Filtered DWI===");
        printGraph();
    }

    // Remove. For debugging purpose. Nothing special
    private void printGraph() {
        for (Integer sender : DWG.keySet()) {
            System.out.println("-------------------------------------");
            List<Edge> temp = new LinkedList<>();
            temp = DWG.get(sender);
            temp.stream().forEach(x -> x.printEdge());
        }
    }

    /**
     * @return ID of every users in DWInteraction Graph, where every element in the set is a User ID
     * in this DWInteractionGraph.
     */
    public Set<Integer> getUserIDs() {
        return new HashSet<>(userSet);
    }

    /**
     * @param sender   the User ID of the sender in the email transaction.
     * @param receiver the User ID of the receiver in the email transaction.
     * @return the number of emails sent from the specified sender to the specified
     * receiver in this DWInteractionGraph.
     */
    public int getEmailCount(int sender, int receiver) {
        int weight = 0;
        for (List data : emailDataWithWeight) {
            if ((int) data.get(SENDER) == sender && (int) data.get(RECEIVER) == receiver) {
                weight = (int) data.get(WEIGHT);
            }
        }
        return weight;
    }

    // Make a data [sender, receiver, weight]
    private void setEmailDataWithWeight(List<List<Integer>> data) {
        emailDataWithWeight = new ArrayList<>();
        userSet = new HashSet<>();
        data.stream().forEach(x -> userSet.add(x.get(SENDER)));
        data.stream().forEach(x -> userSet.add(x.get(RECEIVER)));

        userList = new ArrayList<>(userSet);

        for (Integer sender : userList) {
            for (Integer receiver : userList) {
                List<Integer> tempList = new ArrayList<>();
                Integer weight = 0;
                for (List<Integer> d : data) {
                    if (d.get(SENDER) == sender && d.get(RECEIVER) == receiver) {
                        weight++;
                    }
                }
                if (weight > 0) {
                    tempList.add(sender);
                    tempList.add(receiver);
                    tempList.add(weight);

                    emailDataWithWeight.add(tempList);
                }
            }
        }
    }

    private List<List<Integer>> processData(String fileName) {
        List<List<Integer>> dataInteger = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            for (String fileLine = reader.readLine();
                 fileLine != null;
                 fileLine = reader.readLine()) {
                dataInteger.add(stringToInteger(fileLine));

            }
            reader.close();
        } catch (IOException ioe) {
            System.out.println("Problem reading file!");
        }
        return dataInteger;
    }

    private List<Integer> stringToInteger(String fileLine) {
        List<Integer> integerList = new ArrayList<>();
        String[] fileLineParts = fileLine.split("\\s+");

        for (String fileLinePart : fileLineParts) {
            integerList.add(Integer.parseInt(fileLinePart));
        }

        return integerList;
    }

    // defensive copying return
    protected List<List<Integer>> getDWI_data() {
        return new ArrayList<>(this.emailData);
    }

    /**
     * Given an int array, [t0, t1], reports email transaction details.
     * Suppose an email in this graph is sent at time t, then all emails
     * sent where t0 <= t <= t1 are included in this report.
     *
     * @param timeWindow is an int array of size 2 [t0, t1] where t0<=t1.
     * @return an int array of length 3, with the following structure:
     * [NumberOfSenders, NumberOfReceivers, NumberOfEmailTransactions]
     */
    public int[] ReportActivityInTimeWindow(int[] timeWindow) {
        // I can use DWI constructor with time filter. But I feel like it will mutate original DWI.
        // Not sure. But easy to change implementation later
        int[] report = {0, 0, 0};
        int numEmailTransaction = 0;
        List<List<Integer>> data = new ArrayList<>(emailData);
        List<List<Integer>> timeFilteredData = new ArrayList<>();
        Set<Integer> senders = new HashSet<>();
        Set<Integer> receivers = new HashSet<>();

        for (List l : data) {
            if ((int) l.get(TIME) >= timeWindow[0] && (int) l.get(TIME) <= timeWindow[1]) {
                timeFilteredData.add(l);
                numEmailTransaction++;
            }
        }
        for (List l : timeFilteredData) {
            senders.add((Integer) l.get(SENDER));
            receivers.add((Integer) l.get(RECEIVER));
        }
        report[0] = senders.size();
        report[1] = receivers.size();
        report[2] = numEmailTransaction;
        return report;
    }

    /**
     * Given a User ID, reports the specified User's email transaction history.
     *
     * @param userID the User ID of the user for which the report will be
     *               created.
     * @return an int array of length 3 with the following structure:
     * [NumberOfEmailsSent, NumberOfEmailsReceived, UniqueUsersInteractedWith]
     * If the specified User ID does not exist in this instance of a graph,
     * returns [0, 0, 0].
     */
    public int[] ReportOnUser(int userID) {
        int[] report = {0, 0, 0};
        int numSent = 0;
        int numReceive = 0;
        int subtract = 0;
        int uniqueInteraction = 0;      // Just count number of edges related to the user
        Set<Integer> temp = new HashSet<>();


        for (Integer i : DWG.keySet()) {
            for (Edge e : DWG.get(i)) {
                if (e.getReceiver() == userID) {
                    numReceive += e.getWeight();
                    temp.add((Integer) e.getSender());
                }
                if (e.getSender() == userID) {
                    numSent += e.getWeight();
                    temp.add((Integer) e.getReceiver());
                }
            }
        }

        uniqueInteraction = temp.size();

        report[0] = numSent;
        report[1] = numReceive;
        report[2] = uniqueInteraction;
        return report;
    }

    /**
     * @param N               a positive number representing rank. N=1 means the most active.
     * @param interactionType Represent the type of interaction to calculate the rank for
     *                        Can be SendOrReceive.Send or SendOrReceive.RECEIVE
     * @return the User ID for the Nth most active user in specified interaction type.
     * Sorts User IDs by their number of sent or received emails first. In the case of a
     * tie, secondarily sorts the tied User IDs in ascending order.
     */
    public int NthMostActiveUser(int N, SendOrReceive interactionType) {
        // made new class Element to tie index and value together
        List<Element> sendRanking = new ArrayList<>();
        List<Element> receiveRanking = new ArrayList<>();
        int validSendRank = 0;
        int validReceiveRank = 0;
        int wantedData = 0;

        for (Integer user : userList) {
            int numSend = 0;
            int numReceive = 0;
            for (List dataList : emailDataWithWeight) {
                if (dataList.get(SENDER) == user) {
                    numSend += (int) dataList.get(WEIGHT);
                }
                if (dataList.get(RECEIVER) == user) {
                    numReceive += (int) dataList.get(WEIGHT);
                }
            }
            sendRanking.add(new Element((int) user, numSend));
            receiveRanking.add(new Element((int) user, numReceive));
        }

        sendRanking =
            sendRanking.stream().sorted(Comparator.comparing(Element::getValue).reversed())
                .collect(Collectors.toList());
        receiveRanking =
            receiveRanking.stream().sorted(Comparator.comparing(Element::getValue).reversed())
                .collect(Collectors.toList());

        if (interactionType == SendOrReceive.SEND) {
            validSendRank =
                (int) sendRanking.stream().filter(x -> x.getValue() > 0).count();
            if (N > validSendRank) {
                return -1;
            }
            wantedData = sendRanking.get(N - 1).getIndex();
        }

        if (interactionType == SendOrReceive.RECEIVE) {
            validReceiveRank =
                (int) receiveRanking.stream().filter(x -> x.getValue() > 0).count();
            if (N > validReceiveRank) {
                return -1;
            }
            wantedData = receiveRanking.get(N - 1).getIndex();
        }
        return wantedData;
    }

    /**
     * performs breadth first search on the DWInteractionGraph object
     * to check path between user with userID1 and user with userID2.
     *
     * @param userID1 the user ID for the first user
     * @param userID2 the user ID for the second user
     * @return if a path exists, returns aa list of user IDs
     * in the order encountered in the search.
     * if no path exists, should return null.
     */
    public List<Integer> BFS(int userID1, int userID2) {
        Queue<Integer> q = new LinkedList<>();
        q.add(userID1);
        int node = userID1;
        Set<Integer> nodeVisited = new HashSet<>(node);
        List<Integer> path = new ArrayList<>(node);
        List<Integer> noPath = new ArrayList<>();

        while (!q.isEmpty()) {
            List<Integer> nodeList = new ArrayList<>();
            node = q.poll();
            if (node == userID2) {
                path.add(node);
                return path;
            } else {
                DWG.get(node).forEach(x -> nodeList.add(x.getReceiver()));
                nodeList.forEach(x -> {
                    if (!nodeVisited.contains(x)) {
                        q.add(x);
                        nodeVisited.add(x);
                    }
                });
                if (!path.contains(node)) {
                    path.add(node);
                }
            }
        }

        return noPath;
    }

    /**
     * performs depth first search on the DWInteractionGraph object
     * to check path between user with userID1 and user with userID2.
     *
     * @param userID1 the user ID for the first user
     * @param userID2 the user ID for the second user
     * @return if a path exists, returns aa list of user IDs
     * in the order encountered in the search.
     * if no path exists, should return null.
     */
    public List<Integer> DFS(int userID1, int userID2) {
        Set<Integer> isVisited = new LinkedHashSet<>();
        boolean found = false;

        if (!(userList.contains(userID1) || userList.contains(userID2))) {
            return null;
        }

        found = recursiveDFS(userID1, userID2, isVisited);

        if (!found) {
            return null;
        }

        return new ArrayList<>(isVisited);
    }

    private boolean recursiveDFS(Integer user, Integer targetUser, Set<Integer> isVisited) {
        boolean found = false;
        isVisited.add(user);
        if (user == targetUser) {
            found = true;
        }
        for (Edge adjacent : DWG.get(user)) {
            if (!(isVisited.contains(adjacent.getReceiver())) &&
                !(isVisited.contains(targetUser))) {

                found = recursiveDFS(adjacent.getReceiver(), targetUser, isVisited);
            }

        }
        return found;
    }

    /**
     * Calculate the maximum number of users polluted by the malicious email in N hours
     *
     * @param hours is delaying time of triggering fire wall after the first attack of a hacker
     * @return the maximum number of users that can be polluted in N hours
     */
    public int MaxBreachedUserCount(int hours) {
        int totalSeconds = hours * 3600;
        int maximumInfected = 0;
        List<Integer> timeList = new ArrayList<>();

        getDWI_data().forEach(x -> timeList.add(x.get(TIME)));
        Collections.sort(timeList);

        for (int i = 0; i < timeList.size() - 1; i++) {
            int timeLimit = timeList.get(i) + totalSeconds;
            int j = i + 1;
            int count = 0;
            List<Integer> possibleUsers = new ArrayList<>();
            List<List<Integer>> timeFilteredData;

            while (j < timeList.size() && timeList.get(j) < timeLimit) {
                j++;
            }
            j--;
            int timeI = i;

            timeFilteredData = getFilteredData(timeList.get(i), timeList.get(j));

            timeFilteredData.forEach(x -> {
                if (x.contains(timeList.get(timeI))) {
                    possibleUsers.add(x.get(SENDER));
                }
            });

            for (Integer possibleUser : possibleUsers) {
                count = getMaxCount(possibleUser, timeFilteredData);
                if (count > maximumInfected) {
                    maximumInfected = count;
                }
            }
        }
        return maximumInfected;
    }

    private int getMaxCount(int startUser, List<List<Integer>> timeFilteredData) {
        return BFS_for_MaxBreached(startUser, timeFilteredData).size();
    }

    public List<Integer> BFS_for_MaxBreached(int startUser, List<List<Integer>> timeFilteredData) {
        Queue<Integer> q = new LinkedList<>();
        q.add(startUser);
        int node = startUser;
        Set<Integer> nodeVisited = new HashSet<>(node);
        List<Integer> path = new ArrayList<>(node);

        while (!q.isEmpty()) {
            List<Integer> nodeList = new ArrayList<>();
            node = q.poll();
            int nodeInLambda = node;

            timeFilteredData.forEach(x -> {
                if (x.get(SENDER) == nodeInLambda) {
                    nodeList.add(x.get(RECEIVER));
                }
            });
            nodeList.forEach(x -> {
                if (!nodeVisited.contains(x)) {
                    q.add(x);
                    nodeVisited.add(x);
                }
            });
            if (!path.contains(node)) {
                path.add(node);
            }
        }
        return path;
    }

    private List<List<Integer>> getFilteredData(int timeBegin, int timeEnd) {
        List<List<Integer>> timeFilteredData = new ArrayList<>();
        for (List l : getDWI_data()) {
            if (((int) l.get(TIME) >= timeBegin) && ((int) l.get(TIME) <= timeEnd)) {
                timeFilteredData.add(l);
            }
        }

        return timeFilteredData;
    }
}