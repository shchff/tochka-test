import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class Main {
    // Константы для символов ключей и дверей
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];


    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char)('a' + i);
            DOORS_CHAR[i] = (char)('A' + i);
        }
    }


    // Чтение данных из стандартного ввода
    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;


        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }


        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }


        return maze;
    }

    private static class Edge
    {
        public int to;
        public int dist;
        public Set<Character> doorsRequired;
        private Edge(int to, int dist, Set<Character> doorsRequired)
        {
            this.to = to;
            this.dist = dist;
            this.doorsRequired = doorsRequired;
        }
    }

    private static class Entry
    {
        public int x;
        public int y;
        public int dist;
        public Set<Character> doors;

        public Entry(int x, int y, int dist, Set<Character> doors)
        {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.doors = doors;
        }
    }

    private static class LabyrinthState implements Comparable<LabyrinthState> {
        public int[] robotsPositions;
        public Set<Character> keys;
        public int dist;

        public LabyrinthState(int[] robotsPositions, Set<Character> keys, int dist)
        {
            this.robotsPositions = robotsPositions.clone();
            this.keys = new HashSet<>(keys);
            this.dist = dist;
        }

        @Override
        public int compareTo(LabyrinthState o)
        {
            return Integer.compare(this.dist, o.dist);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (getClass() != o.getClass())
            {
                return false;
            }

            LabyrinthState s = (LabyrinthState) o;
            return Arrays.equals(robotsPositions, s.robotsPositions) && keys.equals(s.keys);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(robotsPositions) * 31 + keys.hashCode();
        }
    }

    private static int solve(char[][] data)
    {
        int rowsLength = data.length;
        int columnsLength = data[0].length;

        List<int[]> robots = new ArrayList<>();
        Map<Character, int[]> keyPositions = new HashMap<>();

        for (int i = 0; i < rowsLength; i++)
        {
            for (int j = 0; j < columnsLength; j++)
            {
                char cell = data[i][j];
                if (cell == '@')
                {
                    robots.add(new int[]{i, j});
                }
                else if (cell >= 'a' && cell <= 'z')
                {
                    keyPositions.put(cell, new int[]{i, j});
                }
            }
        }

        List<Character> keysList = new ArrayList<>(keyPositions.keySet());
        Collections.sort(keysList);

        List<int[]> nodes = new ArrayList<>(robots);
        Map<Character, Integer> keyIndexInNodesArray = new HashMap<>();

        for (int i = 0; i < keysList.size(); i++)
        {
            char key = keysList.get(i);
            nodes.add(keyPositions.get(key));
            keyIndexInNodesArray.put(key, i + 4);
        }

        List<List<Edge>> graph = new ArrayList<>();

        int[] directionsX = {-1, 1, 0, 0};
        int[] directionsY = {0, 0, -1, 1};

        for (int i = 0; i < nodes.size(); i++)
        {
            graph.add(new ArrayList<>());
        }

        for (int node = 0; node < nodes.size(); node++)
        {
            boolean[][] visited = new boolean[rowsLength][columnsLength];
            Queue<Entry> queue = new ArrayDeque<>();

            int startX = nodes.get(node)[0];
            int startY = nodes.get(node)[1];
            visited[startX][startY] = true;
            queue.add(new Entry(startX, startY, 0, new HashSet<>()));

            while (!queue.isEmpty())
            {
                Entry currEntry = queue.poll();
                char cell = data[currEntry.x][currEntry.y];
                Set<Character> doors = new HashSet<>(currEntry.doors);

                if (cell >= 'A' && cell <= 'Z')
                {
                    doors.add(cell);
                }

                if (cell >= 'a' && cell <= 'z')
                {
                    int newFromNode = keyIndexInNodesArray.get(cell);

                    if (newFromNode != node)
                    {
                        graph.get(node).add(new Edge(newFromNode, currEntry.dist, doors));
                    }
                }

                for (int direction = 0; direction < 4; direction++)
                {
                    int newX = currEntry.x + directionsX[direction];
                    int newY = currEntry.y + directionsY[direction];

                    if (newX >= 0 && newX < rowsLength && newY >= 0 && newY < columnsLength
                            && !visited[newX][newY] && data[newX][newY] != '#')
                    {
                        visited[newX][newY] = true;
                        queue.add(new Entry(newX, newY, currEntry.dist + 1, doors));
                    }
                }
            }
        }

        PriorityQueue<LabyrinthState> priorityQueue = new PriorityQueue<>();
        Map<LabyrinthState, Integer> distMap = new HashMap<>();

        LabyrinthState start = new LabyrinthState(new int[] {0, 1, 2, 3}, Collections.emptySet(), 0);
        distMap.put(start, 0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty())
        {
            LabyrinthState state = priorityQueue.poll();

            if (state.dist != distMap.getOrDefault(state, Integer.MAX_VALUE))
            {
                continue;
            }

            if (state.keys.size() == keysList.size())
            {
                return state.dist;
            }

            for (int robot = 0; robot < 4; robot++)
            {
                int currPosition = state.robotsPositions[robot];

                for (Edge edge : graph.get(currPosition))
                {
                    char key = keysList.get(edge.to - 4);

                    if (state.keys.contains(key))
                    {
                        continue;
                    }

                    boolean allDoorsAreOpen = true;
                    for (char door : edge.doorsRequired)
                    {
                        if (!state.keys.contains(Character.toLowerCase(door)))
                        {
                            allDoorsAreOpen = false;
                            break;
                        }
                    }
                    if (!allDoorsAreOpen)
                    {
                        continue;
                    }

                    Set<Character> newKeys = new HashSet<>(state.keys);
                    newKeys.add(key);

                    int[] newPositions = state.robotsPositions.clone();
                    newPositions[robot] = edge.to;

                    LabyrinthState newState = new LabyrinthState(newPositions, newKeys, state.dist + edge.dist);

                    int prevDist = distMap.getOrDefault(newState, Integer.MAX_VALUE);

                    if (newState.dist < prevDist)
                    {
                        distMap.put(newState, newState.dist);
                        priorityQueue.add(newState);
                    }
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = solve(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }
}