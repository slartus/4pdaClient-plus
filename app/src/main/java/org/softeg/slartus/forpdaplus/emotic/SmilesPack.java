package org.softeg.slartus.forpdaplus.emotic;

import org.softeg.slartus.forpdaplus.classes.BbImage;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 20.05.13
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */
public class SmilesPack extends Smiles {

    private final String m_DirPath;

    protected SmilesPack(String cssFilePath) {
        super();
        m_DirPath = cssFilePath;
    }

    @Override
    public String getDirPath() {
        return m_DirPath;
    }

    public String getCssPath() {
        return getDirPath();
    }

    @Override
    public BbImage[] getFilesList() {
        BbImage[] res = new BbImage[size()];
        String path = m_DirPath;
        for (int i = 0; i < size(); i++)

            res[i] = new BbImage(path, this.get(i).FileName, this.get(i).HtmlText);

        return res;
    }
}
