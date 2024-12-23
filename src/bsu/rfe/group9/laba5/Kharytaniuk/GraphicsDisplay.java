package bsu.rfe.group9.laba5.Kharytaniuk;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showHorizontalLines = true;

    private double minX, maxX, minY, maxY, scaleX, scaleY;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Rectangle dragRect = null;

    // Добавляем поле для хранения выделенной точки
    private Point2D.Double highlightedPoint = null;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        //добавили слушатель событий мыши
        addMouseListener(new MouseAdapter() {
            /*mousePressed:
Если нажата правая кнопка мыши — сбрасываем масштабирование (вызывается метод resetScale).
Если нажата левая кнопка мыши — начинаем выделение (isDragging = true) и сохраняем стартовую точку выделения (dragStart).*/
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetScale(); // Восстановление исходного масштаба
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true; // Начало выделения
                    dragStart = e.getPoint();
                }
            }

            /*mouseReleased:
            Когда отпускается левая кнопка мыши, проверяем, была ли выделена область (dragRect).
            Если область выделена, вызываем метод scaleToArea, чтобы увеличить выделенную область графика.*/
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    if (dragRect != null && dragRect.width > 0 && dragRect.height > 0) {
                        scaleToArea(dragRect); // Масштабирование выделенной области
                    }
                    isDragging = false;
                    dragRect = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            /*mouseMoved: Вызывается при движении мыши. Проверяем, находится ли курсор
            над какой-либо точкой (updateHighlightedPoint).*/
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHighlightedPoint(e); // Обновляем выделенную точку при движении мыши
            }

            /*mouseDragged: Во время перетаскивания левой кнопки обновляем
            размеры выделяемого прямоугольника (dragRect) и вызываем repaint для его отображения.*/
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragRect = new Rectangle(dragStart);
                    dragRect.add(e.getPoint()); // Рисование рамки выделения
                    repaint();
                }
            }
        });
    }

    // Метод для обновления выделенной точки
    private void updateHighlightedPoint(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        Point2D.Double newHighlightedPoint = null;

        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (Math.abs(graphPoint.x - mousePoint.x) < 5 && Math.abs(graphPoint.y - mousePoint.y) < 5) {
                newHighlightedPoint = new Point2D.Double(point[0], point[1]);
                break;
            }
        }

        // Если выделенная точка изменилась, обновляем и перерисовываем
        if ((highlightedPoint == null && newHighlightedPoint != null)
                || (highlightedPoint != null && !highlightedPoint.equals(newHighlightedPoint))) {
            highlightedPoint = newHighlightedPoint;
            repaint(); // Перерисовка панели
        }
    }

    private void scaleToArea(Rectangle rect) {
        double newMinX = minX + (rect.x / scaleX);
        double newMaxX = minX + ((rect.x + rect.width) / scaleX);
        double newMinY = maxY - ((rect.y + rect.height) / scaleY);
        double newMaxY = maxY - (rect.y / scaleY);

        minX = newMinX;
        maxX = newMaxX;
        minY = newMinY;
        maxY = newMaxY;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);

        repaint();
    }

    private void resetScale() {
        calculateBounds(); // Восстановление границ
        repaint();
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculateBounds();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        Graphics2D canvas = (Graphics2D) g;
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        if (dragRect != null) { // Рисование рамки выделения
            canvas.setColor(Color.BLACK);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            canvas.draw(dragRect);
        }

        // Рисуем координаты выделенной точки, если она есть
        if (highlightedPoint != null) {
            Point2D.Double graphPoint = xyToPoint(highlightedPoint.x, highlightedPoint.y);
            canvas.setColor(Color.BLACK);
            canvas.drawString(String.format("(%.2f, %.2f)", highlightedPoint.x, highlightedPoint.y),
                    (int) graphPoint.x + 5, (int) graphPoint.y - 5);
        }
    }

    private void calculateBounds() {
        minX = graphicsData[0][0];
        maxX = graphicsData[0][0];
        minY = graphicsData[0][1];
        maxY = graphicsData[0][1];

        for (Double[] point : graphicsData) {
            if (point[0] < minX) minX = point[0];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[1] > maxY) maxY = point[1];
        }

        maxX += maxX * 0.25;
        minX -= maxX * 0.25;
        maxY += maxX * 0.2;
        minY -= maxX * 0.1;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f)); // Толщина осей
        canvas.setColor(Color.BLACK); // Цвет осей
        canvas.setFont(new Font("Arial", Font.PLAIN, 12)); // Шрифт для подписей осей

        // Получаем FontMetrics для текущего шрифта
        FontMetrics metrics = canvas.getFontMetrics();

        // Ось Y
        if (minX <= 0 && maxX >= 0) { // Если ось пересекает ось X (горизонтальную)
            // Рисуем вертикальную ось Y
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            // Стрелка на оси Y
            GeneralPath arrowY = new GeneralPath();
            Point2D.Double lineEndY = xyToPoint(0, maxY);
            arrowY.moveTo(lineEndY.getX(), lineEndY.getY());
            arrowY.lineTo(arrowY.getCurrentPoint().getX() + 5, arrowY.getCurrentPoint().getY() + 20);
            arrowY.lineTo(arrowY.getCurrentPoint().getX() - 10, arrowY.getCurrentPoint().getY());
            arrowY.closePath();
            canvas.draw(arrowY);
            canvas.fill(arrowY);

            // Подпись оси Y
            Rectangle2D boundsY = metrics.getStringBounds("y", canvas);
            Point2D.Double labelPosY = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPosY.getX() + 10, (float) (labelPosY.getY() - boundsY.getY()));

            // Подписи для maxY и minY
            Point2D.Double maxYPoint = xyToPoint(0, maxY);
            Point2D.Double minYPoint = xyToPoint(0, minY);
            canvas.drawString(String.format("maxY (%.2f)", maxY), (float) maxYPoint.getX() + 50, (float) maxYPoint.getY() + 20);
            canvas.drawString(String.format("minY (%.2f)", minY), (float) minYPoint.getX() + 10, (float) minYPoint.getY());
        }

        // Ось X
        if (minY <= 0 && maxY >= 0) { // Если ось пересекает ось Y (вертикальную)
            // Рисуем горизонтальную ось X
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            // Стрелка на оси X
            GeneralPath arrowX = new GeneralPath();
            Point2D.Double lineEndX = xyToPoint(maxX, 0);
            arrowX.moveTo(lineEndX.getX(), lineEndX.getY());
            arrowX.lineTo(arrowX.getCurrentPoint().getX() - 20, arrowX.getCurrentPoint().getY() - 5);
            arrowX.lineTo(arrowX.getCurrentPoint().getX(), arrowX.getCurrentPoint().getY() + 10);
            arrowX.closePath();
            canvas.draw(arrowX);
            canvas.fill(arrowX);

            // Подпись оси X
            Rectangle2D boundsX = metrics.getStringBounds("x", canvas);
            Point2D.Double labelPosX = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPosX.getX() - boundsX.getWidth() - 10),
                    (float) (labelPosX.getY() + boundsX.getY()));

            // Подписи для minX и maxX
            Point2D.Double minXPoint = xyToPoint(minX, 0);
            Point2D.Double maxXPoint = xyToPoint(maxX, 0);
            canvas.drawString(String.format("minX (%.2f)", minX), (float) minXPoint.getX(), (float) minXPoint.getY() + 20);
            canvas.drawString(String.format("maxX (%.2f)", maxX), (float) maxXPoint.getX(), (float) maxXPoint.getY() + 20);

            // Дополнительно можно добавить маркеры для значений на оси X
            for (double x = minX; x <= maxX; x += (maxX - minX) / 5) {
                Point2D.Double point = xyToPoint(x, 0);
                canvas.setColor(Color.RED); // Маркеры для оси X
                canvas.fillOval((int) point.x - 2, (int) point.y - 2, 4, 4); // Маленькие круги для маркеров
            }
        }
    }




    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f));
        canvas.setColor(Color.BLUE);
        GeneralPath graph = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i == 0) graph.moveTo(point.x, point.y);
            else graph.lineTo(point.x, point.y);
        }
        canvas.draw(graph);
    }

    protected void paintMarkers(Graphics2D canvas) {
        // Настройки для рисования
        canvas.setStroke(new BasicStroke(1.0f)); // Толщина линий

        for (Double[] point : graphicsData) {
            // Определяем цвет маркера (синий для чётных, красный для нечётных значений)
            // Преобразуем координаты точки
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.setColor(point[1] > (maxY + minY) / 2 ? Color.BLUE : Color.RED);

            double radius = 4; // Радиус круга
            double lineLength = 5; // Длина линий, пересекающих круг

            // Создаём пустой круг (только граница)
            Ellipse2D.Double circle = new Ellipse2D.Double(
                    center.x - radius, center.y - radius, 2 * radius, 2 * radius);
            canvas.draw(circle); // Рисуем круг

            // Рисуем горизонтальную линию
            Line2D.Double horizontalLine = new Line2D.Double(
                    center.x - lineLength / 2, center.y,
                    center.x + lineLength / 2, center.y);
            canvas.draw(horizontalLine);

            // Рисуем вертикальную линию
            Line2D.Double verticalLine = new Line2D.Double(
                    center.x, center.y - lineLength / 2,
                    center.x, center.y + lineLength / 2);
            canvas.draw(verticalLine);
        }

        // Добавить точку (0, 0) на график, если она видима
        Point2D.Double origin = xyToPoint(0, 0);
        if (minX <= 0 && maxX >= 0 && minY <= 0 && maxY >= 0) {
            canvas.setColor(Color.GREEN); // Цвет для точки (0, 0)
            Ellipse2D.Double originMarker = new Ellipse2D.Double(
                    origin.x - 5, origin.y - 5, 10, 10); // Радиус точки = 5
            canvas.fill(originMarker); // Рисуем заполненную окружность
        }


    }
}
