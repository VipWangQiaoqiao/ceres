package com.bc.ceres.swing.figure.support;

import static com.bc.ceres.swing.figure.support.StyleDefaults.*;
import com.bc.ceres.swing.figure.AbstractHandle;
import com.bc.ceres.swing.figure.Figure;
import com.bc.ceres.swing.figure.FigureStyle;

import java.awt.Shape;
import java.awt.geom.Path2D;


public class VertexHandle extends AbstractHandle {
    private final int vertexIndex;

    public VertexHandle(Figure figure,
                        int vertexIndex,
                        FigureStyle style,
                        FigureStyle selectedStyle) {
        super(figure, style, selectedStyle);
        this.vertexIndex = vertexIndex;
        setHandleShape();
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    protected Shape createHandleShape() {
        final double[] segment = getFigure().getVertex(vertexIndex);
        double x = segment[0];
        double y = segment[1];
        Path2D path = new Path2D.Double();
        path.moveTo(x, y - 0.5 * VERTEX_HANDLE_SIZE);
        path.lineTo(x + 0.5 * VERTEX_HANDLE_SIZE, y);
        path.lineTo(x, y + 0.5 * VERTEX_HANDLE_SIZE);
        path.lineTo(x - 0.5 * VERTEX_HANDLE_SIZE, y);
        path.closePath();
        return path;
    }

    @Override
    public void move(double dx, double dy) {
        final double[] segment = getFigure().getVertex(vertexIndex);
        if (segment != null) {
            segment[0] += dx;
            segment[1] += dy;
            getFigure().setVertex(vertexIndex, segment);
        }
    }
}