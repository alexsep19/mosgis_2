define ([], function () {

    $_DO.choose_tab_vc_oh_wk_type = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('vc_oh_wk_type.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'voc_overhaul_work_types'}, {}, function (data) {

            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1,
                vc_nsi_218: 1,
                vc_async_entity_states: 1,
                vc_orgs: 1,
            })

            data.item.status_label = data.vc_gis_status [data.item.id_owt_status]
            data.item.org_label = data.item ['vc_orgs.label']
            data.item.voc_code = data.item.code ? data.item.code : 'Отсутствует'
            data.item.actuality = data.item.isactual ? 'Актуально' : 'Не актуально'

            var it = data.item

            it._can = {cancel: 1}

            var is_locked = it.is_deleted || !$_USER.role.nsi_20_7

            if (!is_locked) {

                switch (it.id_owt_status) {
                    case 11:
                    case 14:
                    case 34:
                        it._can.edit = 1
                }

                it._can.delete = it._can.update = it._can.edit

                switch (it.id_owt_status) {
                    case 40:
                        it._can.alter = 1
                }
                
            }

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})