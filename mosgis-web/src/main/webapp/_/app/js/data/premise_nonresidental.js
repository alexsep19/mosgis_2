define ([], function () {

    $_DO.choose_tab_premise_nonresidental = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('premise_nonresidental.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'premises_nonresidental'}, {}, function (data) {

            add_vocabularies (data, {
                "vc_nsi_17": 1,
                "vc_nsi_330": 1,
                "vc_house_status": 1,
            })

            var it = data.item

            it.label = it.premisesnum

            data.active_tab = localStorage.getItem ('premise_nonresidental.active_tab') || 'premise_nonresidental_common'

            it.address = it ['tb_houses.address']

            it._can = {}

            if (($_USER.role.admin || (data.cach && data.cach.is_own) || is_own_srca(data) && it.uuid_org == $_USER.uuid_org) && !it.is_deleted) {
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