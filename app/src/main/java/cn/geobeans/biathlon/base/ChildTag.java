package cn.geobeans.biathlon.base;

import androidx.annotation.NonNull;

/**
 * @Author: baixm
 * @Date: 2019/12/13
 */
public class ChildTag implements Comparable<ChildTag> {
    private String name;
    private boolean tag;

    public ChildTag(String name, boolean tag) {
        this.name = name;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTag() {
        return tag;
    }

    public void setTag(boolean tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(@NonNull ChildTag o) {
        return this.getName().compareTo(o.getName());
    }
}
