package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Model extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false; // 游戏是否正在进行
    private boolean dying = false; // 吃豆人是否处于死亡状态

    private final int BLOCK_SIZE = 24;    // 每个格子的大小
    private final int N_BLOCKS = 15;      // 游戏界面的格子数（15x15）
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;  // 界面总大小
    private final int MAX_GHOSTS = 12;    // 最大幽灵数量
    private final int PACMAN_SPEED = 6;   // 吃豆人的移动速度

    private int N_GHOSTS_NOW = 3;   // 当前幽灵数量
    private int N_GHOSTS = 3;
    private int lives, score;     // 吃豆人的生命数和分数
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;   // 幽灵的位置和方向
    private boolean[] ghostFrightened;
    private boolean[] ghostRemoved = new boolean[MAX_GHOSTS];  // 存储每个幽灵是否已经被移除

    private Image heart, ghost1, ghost2, ghostscared1, ghostscared2;
    private Image up1, up2, up3, down1, down2, down3, left1, left2, left3, right1, right2, right3;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;  // 吃豆人的位置和方向
    private int req_dx, req_dy;

    private int frightFruit_x, frightFruit_y;  // FrightFruit 的位置
    private boolean frightFruitEaten = false;  // 是否已经吃掉 FrightFruit



    private final short levelData[] = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };



    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};   // 可用速度
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;  // 动态地图状态
    private Timer timer;

    public Model() {
        loadImages();        // 加载图片资源
        initVariables();     // 初始化变量
        addKeyListener(new TAdapter());  // 添加键盘监听器
        setFocusable(true);  // 使面板能够响应键盘事件
        initGame();          // 初始化游戏
    }


    private void loadImages() {
        down1 = new ImageIcon(getClass().getResource("/PacMan2down.gif")).getImage();
        down2 = new ImageIcon(getClass().getResource("/PacMan3down.gif")).getImage();
        down3 = new ImageIcon(getClass().getResource("/PacMan4down.gif")).getImage();
        up1 = new ImageIcon(getClass().getResource("/PacMan2up.gif")).getImage();
        up2 = new ImageIcon(getClass().getResource("/PacMan3up.gif")).getImage();
        up3 = new ImageIcon(getClass().getResource("/PacMan4up.gif")).getImage();
        left1 = new ImageIcon(getClass().getResource("/PacMan2left.gif")).getImage();
        left2 = new ImageIcon(getClass().getResource("/PacMan3left.gif")).getImage();
        left3 = new ImageIcon(getClass().getResource("/PacMan4left.gif")).getImage();
        right1 = new ImageIcon(getClass().getResource("/PacMan2right.gif")).getImage();
        right2 = new ImageIcon(getClass().getResource("/PacMan3right.gif")).getImage();
        right3 = new ImageIcon(getClass().getResource("/PacMan4right.gif")).getImage();
        ghost1 = new ImageIcon(getClass().getResource("/Ghost1.gif")).getImage();
        ghost2 = new ImageIcon(getClass().getResource("/Ghost2.gif")).getImage();
        ghostscared1 = new ImageIcon(getClass().getResource("/GhostScared1.gif")).getImage();
        ghostscared2 = new ImageIcon(getClass().getResource("/GhostScared2.gif")).getImage();
        heart = new ImageIcon(getClass().getResource("/PacMan1.gif")).getImage();

    }
    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        ghostFrightened = new boolean[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

//负责控制吃豆人移动、幽灵移动，以及游戏状态检查
    private void playGame(Graphics2D g2d) {

        if (dying) {

            death(g2d);

        } else {

            movePacman(); //处理吃豆人移动逻辑
            drawPacman(g2d);
            drawFrightFruit(g2d);
            checkFrightFruit();
            moveGhosts(g2d); //处理幽灵移动逻辑，包括自动路径选择。
            checkMaze(); //检查迷宫状态（是否清空了豆子）。
        }
    }
    //显示游戏开始
    private void showIntroScreen(Graphics2D g2d) {

        String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (SCREEN_SIZE)/4, 150);
    }
    //绘制分数和生命
    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true; //假设迷宫已完成

        while (i < N_GHOSTS && finished) {
            if (!ghostRemoved[i]) {
                finished = false;
                break;
            }
            i++;
        }
        //如果所有Ghost已被消灭
        if (finished) {
            inGame = false;
            score += 50;
            // 显示对话框
            int response = javax.swing.JOptionPane.showOptionDialog(
                    this,
                    "闯关成功！是否前往下一个关卡？",
                    "闯关成功！",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"是", "否"},
                    "是"
            );

            if (response == javax.swing.JOptionPane.YES_OPTION) {
                N_GHOSTS_NOW = N_GHOSTS + 2;
            } else {
                System.exit(0); // 退出程序
            }

            for (int j = 0; j < N_GHOSTS_NOW; j++)
                ghostRemoved[j] = false;
            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS += 2;
            }
            if (currentSpeed < maxSpeed) {
                currentSpeed++;
                initLevel();
            } else {
                initLevel();
            }

        }
    }

    private void death(Graphics2D g2d) {

        lives--;

        if (lives == 0) {
            inGame = false;

            // 显示对话框
            int response = javax.swing.JOptionPane.showOptionDialog(
                    this,
                    "游戏结束，是否重新开始游戏？",
                    "Game Over",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"是", "否"},
                    "是"
            );

            if (response == javax.swing.JOptionPane.YES_OPTION) {
                N_GHOSTS_NOW = N_GHOSTS;
                initGame(); // 重新初始化游戏
            } else {
                System.exit(0); // 退出程序
            }
        }

        continueLevel();
    }

    private Timer frightFruitTimer;  // 定义一个成员定时器
    private int cnt = 0;  // 用于跟踪当前的帧
    private final int ANIMATION_SPEED1 = 100;  // 控制帧速率

    private void moveGhosts(Graphics2D g2d) {
        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS_NOW; i++) {
            // 检查幽灵是否被移除，如果已经移除则跳过
            if (ghostRemoved[i]) {
                continue;
            }

            // 检查幽灵是否在迷宫的格子中央
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);
                count = 0;

                // 正常状态下的移动逻辑，检查四个方向是否有墙并设置方向
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {
                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }
                } else {
                    count = (int) (Math.random() * count);
                    if (count > 3) {
                        count = 3;
                    }
                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }

            // 更新幽灵位置
            ghost_x[i] += ghost_dx[i] * ghostSpeed[i];
            ghost_y[i] += ghost_dy[i] * ghostSpeed[i];
            if (cnt >= ANIMATION_SPEED1) {
                cnt = 0;  // 重置帧计数器
            } else {
                cnt++;  // 增加帧计数器
            }
            // 根据幽灵状态绘制
            Image ghostImageToDraw;
            if (cnt <= ANIMATION_SPEED1 / 2)
                ghostImageToDraw = ghost1;
            else
                ghostImageToDraw = ghost2;
            if (ghostFrightened[i]) {
                if (cnt <= ANIMATION_SPEED1 / 2)
                    ghostImageToDraw = ghostscared1;  // 如果幽灵处于惊吓状态，使用 ghostscared
                else
                    ghostImageToDraw = ghostscared2;
            }

            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1, ghostImageToDraw);

            // 检查幽灵与玩家碰撞
            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {
                if (!ghostFrightened[i]) {
                    // 如果幽灵没有处于惊吓状态，玩家被捕获
                    dying = true;
                } else {
                    // 如果幽灵处于惊吓状态，消灭幽灵
                    ghostRemoved[i] = true;  // 设置该幽灵被移除
                    ghostFrightened[i] = false;
                }
            }
        }
    }

    private void checkFrightFruit() {
        if (pacman_x > (frightFruit_x - 12) && pacman_x < (frightFruit_x + 12)
                && pacman_y > (frightFruit_y - 12) && pacman_y < (frightFruit_y + 12)
                && !frightFruitEaten) {

            // 玩家吃掉 FrightFruit，设置幽灵为惊吓状态
            frightFruitEaten = true;
            generateRandomFrightFruit();  // 生成新的 FrightFruit

            for (int i = 0; i < N_GHOSTS_NOW; i++) {
                ghostFrightened[i] = true;  // 将所有幽灵设置为惊吓状态
            }

            // 设置一个计时器，3秒后幽灵恢复正常状态
            if (frightFruitTimer != null && frightFruitTimer.isRunning()) {
                frightFruitTimer.stop();  // 如果定时器已经在运行，先停止
            }

            frightFruitTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < N_GHOSTS_NOW; i++) {
                        ghostFrightened[i] = false;  // 3秒后将所有幽灵恢复正常
                    }
                }
            });

            frightFruitTimer.start();  // 启动定时器
        }
    }



    private void drawGhost(Graphics2D g2d, int x, int y, Image ghostImage) {
        g2d.drawImage(ghostImage, x, y, this);
    }

    // 生成随机 FrightFruit 的方法
    private void generateRandomFrightFruit() {
        List<Integer> validPositions = new ArrayList<>(); // 使用 java.util.List

        // 找出所有符合条件的位置
        for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            if ((screenData[i] & 16) != 0) {  // 判断该位置是否能生成 FrightFruit
                validPositions.add(i);
            }
        }

        // 随机选择一个位置生成 FrightFruit
        if (!validPositions.isEmpty()) {
            int randomIndex = (int) (Math.random() * validPositions.size());  // 随机索引
            int position = validPositions.get(randomIndex);  // 获取该位置

            // 计算 FrightFruit 的 x, y 位置
            frightFruit_x = (position % N_BLOCKS) * BLOCK_SIZE;
            frightFruit_y = (position / N_BLOCKS) * BLOCK_SIZE;
            frightFruitEaten = false;
        }
    }
    // 在绘制过程中显示 FrightFruit
    private void drawFrightFruit(Graphics2D g2d) {
        if (!frightFruitEaten) {
            g2d.setColor(new Color(255, 105, 180)); // 选择颜色
            g2d.fillOval(frightFruit_x + 8, frightFruit_y + 8, 12, 12); // 绘制 FrightFruit
        }
    }
    private void movePacman() {

        int pos;
        short ch;
        //检查 Pacman 是否在网格边界上
        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];
            //吃豆子, ch & 16 检查是否存在豆子, ch & 15 保留其他状态
            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }
            //控制 Pacman 的移动方向
            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                }
            }

            // 检查是否卡住（站着不动）
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;
                pacmand_y = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private int frame = 0;  // 用于跟踪当前的帧
    private final int ANIMATION_SPEED2 = 9;  // 控制帧速率

    private void drawPacman(Graphics2D g2d) {
        // 每隔一段时间切换帧
        if (frame >= ANIMATION_SPEED2) {
            frame = 0;  // 重置帧计数器
        } else {
            frame++;  // 增加帧计数器
        }
        if (req_dx == -1) {  // 向左移动
            if (frame == 0) {
                g2d.drawImage(left1, pacman_x + 1, pacman_y + 1, this);
            } else if (frame == 1) {
                g2d.drawImage(left2, pacman_x + 1, pacman_y + 1, this);
            } else {
                g2d.drawImage(left3, pacman_x + 1, pacman_y + 1, this);
            }
        } else if (req_dx == 1) {  // 向右移动
            if (frame == 0) {
                g2d.drawImage(right1, pacman_x + 1, pacman_y + 1, this);
            } else if (frame == 1) {
                g2d.drawImage(right2, pacman_x + 1, pacman_y + 1, this);
            } else {
                g2d.drawImage(right3, pacman_x + 1, pacman_y + 1, this);
            }
        } else if (req_dy == -1) {  // 向上移动
            if (frame == 0) {
                g2d.drawImage(up1, pacman_x + 1, pacman_y + 1, this);
            } else if (frame == 1) {
                g2d.drawImage(up2, pacman_x + 1, pacman_y + 1, this);
            } else {
                g2d.drawImage(up3, pacman_x + 1, pacman_y + 1, this);
            }
        } else {  // 向下移动
            if (frame == 0) {
                g2d.drawImage(down1, pacman_x + 1, pacman_y + 1, this);
            } else if (frame == 1) {
                g2d.drawImage(down2, pacman_x + 1, pacman_y + 1, this);
            } else {
                g2d.drawImage(down3, pacman_x + 1, pacman_y + 1, this);
            }
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(135, 206, 235));
                g2d.setStroke(new BasicStroke(5));
                //当前位置是空的则填充蓝色矩形
                if ((levelData[i] == 0)) {
                    g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }
                //绘制豆子
                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255, 255, 0));
                    g2d.fillOval(x + 10, y + 10, 4, 4);
                }

                i++;
            }
        }
    }

    private void initGame() {

        lives = 3;
        score = 0;
        initLevel();
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; //start position
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7 * BLOCK_SIZE;  //start position
        pacman_y = 11 * BLOCK_SIZE;
        pacmand_x = 0;	//reset direction move
        pacmand_y = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }
        //确保在双缓冲模式下，所有的绘图内容都会同步到屏幕上。这能避免屏幕闪烁
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
        // 根据键盘输入更新吃豆人的方向
            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}




package game;

import javax.swing.JFrame;

public class Pacman extends JFrame{

    public Pacman() {
        add(new Model());
    }


    public static void main(String[] args) {
        Pacman pac = new Pacman();
        pac.setVisible(true);
        pac.setTitle("Pacman");
        pac.setSize(380,420);
        pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pac.setLocationRelativeTo(null);

    }

}