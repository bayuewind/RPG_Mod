package rpgclasses.content.player.SkillsAndAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SkillsList<T extends Skill> {
    private final Map<String, T> map = new HashMap<>();
    private final List<T> list = new ArrayList<>();

    public SkillsList() {
    }

    public T addSkill(T skill) {
        skill.id = list.size();
        map.put(skill.stringID, skill);
        list.add(skill);
        return skill;
    }

    public T get(int i) {
        return list.get(i);
    }

    public T get(String str) {
        return map.get(str);
    }

    public int size() {
        return list.size();
    }

    public void each(Consumer<? super T> consumer) {
        list.forEach(consumer);
    }

    public List<T> getList() {
        return list;
    }
}
