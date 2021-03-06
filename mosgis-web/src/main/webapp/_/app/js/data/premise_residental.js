define ([], function () {

    $_DO.choose_tab_premise_residental = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('premise_residental.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'premises_residental'}, {}, function (data) {

            add_vocabularies (data, {
                "tb_entrances": 1,
                "vc_house_status": 1,
                "vc_nsi_30": 1,
                "vc_nsi_273": 1,
                "vc_nsi_330": 1,
            })

            var type = data.vc_nsi_30 [data.item.code_vc_nsi_30] || 'помещение'
            
            var prefix = type.indexOf ('вартира') < 0 ? type : 'кв.'
            
            var it = data.item
            
            it.label = prefix + ' ' + it.premisesnum

            it.address = data.item ['tb_houses.address']

            it._can = {}

            data.is_own_srca = is_own_srca(data)

            if (($_USER.role.admin || (data.cach && data.cach.is_own) || data.is_own_srca && it.uuid_org == $_USER.uuid_org) && !it.is_deleted) {
                it._can.edit   = 1 - it.is_annuled
                it._can.delete = it._can.update = it._can.cancel = it._can.edit                
                it._can.annul = 1 ? !data.item.is_annuled && data.item.id_status == 20 : 0
                it._can.restore = it.is_annuled_in_gis
            }

            $('body').data ('data', data)

            done (data)        
            
        })
        
    }

})