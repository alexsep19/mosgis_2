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
            data.item.servicename = '"' + data.item.servicename + '"'
            data.item.work_group = data.vc_nsi_218 [data.item.code_vc_nsi_218]
            data.item.actuality = data.item.isactual ? 'Актуально' : 'Не актуально'

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})