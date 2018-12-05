define ([], function () {

    $_F5 = function (data) {

        __d(data)

        w2ui ['voc_organization_legal_form'].record = data

        w2ui ['voc_organization_legal_form'].refresh()
    }

    return function (data, view) {

        var it = data.item
        
        it.label_delegated = data.is_delegated ? 'Да' : 'Нет'        

        it.vc_nsi_20 = data.vc_orgs_nsi_20
            .map (function (r) {return data.vc_nsi_20 [r.code]})
            .sort ()
            .join (',<br>')

        var $html = fill (view, it)

        var layout = w2ui ['voc_organization_legal_layout']

        var $panel = $(layout.el ('main'))

        $panel.empty().append($html);

        $panel.w2reform({
            name: 'voc_organization_legal_form',
            record: it,
            onRefresh: function () {
                if (data.is_delegated && is_own) clickOn ($('#delegated'), function () {
                    w2ui ['topmost_layout'].get ('main').tabs.click ('voc_organization_legal_access_requests')
                })
            }
        })
        
        var is_own = $_USER.role.admin || $_USER.uuid_org == $_REQUEST.id

        $('#container').w2relayout({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'voc_organization_legal_users', caption: 'Учётные записи', off: !is_own},
                            {id: 'voc_organization_legal_access_requests', caption: 'Делегированные права', off: !data.is_delegated || !is_own},
                            {id: 'voc_organization_legal_log', caption: 'История'},
                        ].filter(not_off),

                        onClick: $_DO.choose_tab_voc_organization_legal

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'voc_organization_legal.active_tab')
            },

        });

        if (it.id_log && it.out_soap && it ['out_soap.id_status'] != 3) {

            w2utils.lock ($('#the_form'), {
                msg     : 'Ждём ответ ГИС ЖКХ...',
                spinner : false,
            })

            setTimeout (reload_page, 2000)

        }

        var charter_uuid = it ['charter.uuid']

        if (charter_uuid && ($_USER.uuid_org == it.uuid || $_USER.role.admin || $_USER.role.nsi_20_4)) {

            clickOn ($('div[data-text=stateregistrationdate]'), function () {
                openTab ('/charter/' + charter_uuid)
            })

        }

        $_F5(it)

    }

})