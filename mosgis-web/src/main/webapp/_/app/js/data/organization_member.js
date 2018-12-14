define ([], function () {

    $_DO.choose_tab_organization_member = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('organization_member.active_tab', name)

        use.block (name)

    }

    return function (done) {

        query ({type: 'organization_members'}, {}, function (data) {

            add_vocabularies (data, {
                vc_actions: 1,
                vc_org_prtcps: 1
            })

            var it = data.item

            it.label = it.label || it ['org.label'] || it ['person.label']
            it['org_auth.label'] = it['org_auth.label'] || 'Администратор'

            it._can = {}

            if (($_USER.role.admin || (data.cach && data.cach.is_own)) && !it.is_deleted) {
                it._can.edit   = 1
                it._can.delete = it._can.update = it._can.cancel = it._can.edit
            }

            $('body').data ('data', data)

            done (data)

        })

    }

})