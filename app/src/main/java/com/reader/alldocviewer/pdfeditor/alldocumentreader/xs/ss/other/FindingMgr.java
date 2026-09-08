/*
 * 文件名称:          SearchCell.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:08:32
 */
package com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.other;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.model.baseModel.Cell;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.model.baseModel.Row;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.model.baseModel.Sheet;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.util.ModelUtil;


/**
 * Find cells which contain search text.
 */
public class FindingMgr
{
    public FindingMgr()
    {
    }

    /**
     * Search the whole sheet from the first cell (used when switching sheets).
     */
    public Cell findCellFromBeginning(Sheet sheet, String searchValue)
    {
        if (searchValue == null || searchValue.trim().length() == 0 || sheet == null)
        {
            return null;
        }
        this.sheet = sheet;
        this.value = searchValue.trim();
        String valueLower = this.value.toLowerCase();

        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++)
        {
            Row row = sheet.getRow(i);
            if (row == null)
            {
                continue;
            }
            for (int j = row.getFirstCol(); j <= row.getLastCol(); j++)
            {
                findedCell = row.getCell(j);
                if (matchesCell(findedCell, valueLower))
                {
                    return findedCell;
                }
            }
        }
        findedCell = null;
        return null;
    }
    
    /**
     * find cells which contain interesting contents
     * @param value
     * @return
     */
    public Cell findCell(Sheet sheet, String value)
    {     
        if (value == null || value.trim().length() == 0 || sheet == null)
        {
            return null;
        }
        this.sheet = sheet;
        this.value = value.trim();
        String valueLower = this.value.toLowerCase();

        Row row;
        //search from current active cell
        for(int i = sheet.getActiveCellRow(); i <= sheet.getLastRowNum(); i++)
        {
            row = sheet.getRow(i);
            if(row == null)
            {
                continue;
            }
            
            int j = (i == sheet.getActiveCellRow()) ? sheet.getActiveCellColumn() : row.getFirstCol();
            for(; j <= row.getLastCol(); j++)
            {
                findedCell = row.getCell(j);
                if (matchesCell(findedCell, valueLower))
                {
                    return findedCell;
                }
            }
        }
        
        //reached to the end of document, then search from the begin of the document
        for(int i = sheet.getFirstRowNum(); i <= sheet.getActiveCellRow(); i++)
        {
            row = sheet.getRow(i);
            if(row == null)
            {
                continue;
            }
            
            int j = row.getFirstCol();
            int lastCol = (i == sheet.getActiveCellRow()) ? sheet.getActiveCellColumn() - 1 : row.getLastCol();
            for(; j <= lastCol; j++)
            {
                findedCell = row.getCell(j);
                if (matchesCell(findedCell, valueLower))
                {
                    return findedCell;
                }
            }
        }
        
        return null;
    }
    
    public Cell findBackward()
    {
        if(findedCell == null || value == null || sheet == null)
        {
            return null;
        }
        
        String valueLower = value.toLowerCase();
        Row row;
        Cell cell;
        for(int i = findedCell.getRowNumber(); i >= sheet.getFirstRowNum(); i--)
        {
            row = sheet.getRow(i);
            if(row == null)
            {
                continue;
            }
            
            int j = (i == findedCell.getRowNumber()) ? findedCell.getColNumber() - 1 : row.getLastCol();
           
            
            for(; j >= row.getFirstCol(); j--)
            {
                cell = row.getCell(j);
                if (matchesCell(cell, valueLower))
                {
                    findedCell = cell;
                    return findedCell;
                }
            }
        }           
        
        return null;
    }
    
    public Cell findForward()
    {
        if(findedCell == null || value == null || sheet == null)
        {
            return null;
        }
        
        String valueLower = value.toLowerCase();
        Row row;
        Cell cell;
        for(int i = findedCell.getRowNumber(); i <= sheet.getLastRowNum(); i++)
        {
            row = sheet.getRow(i);
            if(row == null)
            {
                continue;
            }
            
            int j = (i == findedCell.getRowNumber()) ? findedCell.getColNumber() + 1 : row.getFirstCol();
           
            
            for(; j <= row.getLastCol(); j++)
            {
                cell = row.getCell(j);
                if (matchesCell(cell, valueLower))
                {
                    findedCell = cell;
                    return findedCell;
                }
            }
        }           
        
        return null;
    }

    public String getSearchValue()
    {
        return value;
    }

    public Cell getFindedCell()
    {
        return findedCell;
    }

    public void setFindState(Sheet sheet, String searchValue, Cell cell)
    {
        this.sheet = sheet;
        this.value = searchValue == null ? null : searchValue.trim();
        this.findedCell = cell;
    }
    
    /**
     * 
     */
    public void dispose()
    {
        sheet = null;
        value = null;
        findedCell = null;
    }

    private boolean matchesCell(Cell cell, String valueLower)
    {
        if (cell == null)
        {
            return false;
        }
        String cellContent = ModelUtil.instance().getSearchableCellContent(sheet.getWorkbook(), cell);
        return cellContent != null && cellContent.toLowerCase().contains(valueLower);
    }
    
    private Sheet sheet;
    //finding value
    private String value;
    
    //finded cell
    private Cell findedCell;
    
}
