define ([], function () {

    $_DO.choose_tab_mgmt_contract_object = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('mgmt_contract_object.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'contract_objects'}, {}, function (data) {
        
            add_vocabularies (data, {
                vc_contract_doc_types: 1,
                vc_nsi_3: 1,
                tb_add_services: 1,
                vc_gis_status: 1,
                vc_actions: 1,
            })
            
            var it = data.item

            it._can = {}
            
            var is_locked = it.is_deleted || it.isblocked || it.isconflicted
            
            if (!is_locked) switch (it.id_ctr_status_gis) {
                case 20:
                case 70:
                case 110:
                    is_locked = true
            }
            
            var is_own = $_USER.role.admin || ($_USER.role.nsi_20_1 && it ['ctr.uuid_org'] == $_USER.uuid_org)

            if (!is_locked && is_own) {
            
                switch (it ["ctr.id_ctr_status"]) {

                    case 10:
                    case 11:
                        it._can.edit    = 1
                        if (it.id_ctr_status_gis == 40 && !it.is_annuled) it._can.annul = 1
                        break;
                        
                }

                if (!it.contractobjectversionguid) it._can.delete = 1

                it._can.update = it._can.cancel = it._can.edit

            }

            if (is_own && it.id_ctr_status_gis == 40 && it ["ctr.id_ctr_status_gis"] == 40) {
                it._can.edit_work_list = 1                
            }

            if (it.is_deleted == 0 && !it ['house.uuid'] && it.id_ctr_status_gis != 110 && ($_USER.role.admin || it ['ctr.uuid_org'] == $_USER.uuid_org)) it._can.create_house = 1
            
            var dt = new Date (data.item.startdate + 'Z')

            function dtIso      () {return dt.toISOString ().substr (0, 10)}
            function dtIncMonth () {dt.setMonth (dt.getMonth () + 1)}

            var ms_to = new Date (data.item.enddate + 'Z').getTime ()

            dt.setDate (1)

            data.periods = []

            while (dt.getTime () <= ms_to) {
            
                dtIncMonth ()
                dt.setDate (0)
                
                data.periods.push ({
                    id: dtIso (),
                    text: w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear (),
                })

                dt.setDate (1)
                dtIncMonth ()

                if (dt.getTime () > ms_to) break

            }
            
            $('body').data ('data', data)

            done (data)
                
        })    

    }

})