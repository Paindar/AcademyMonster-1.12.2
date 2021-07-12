package cn.paindar.academymonster.ability.api;

import cn.paindar.academymonster.ability.instance.MonsterSkillInstance;

public class SkillRuntime {
    public final MonsterSkillInstance instance;
    public SkillRuntime(MonsterSkillInstance instance)
    {
        this.instance = instance;
    }

    public void tick() {
        instance.tick();
    }
}
