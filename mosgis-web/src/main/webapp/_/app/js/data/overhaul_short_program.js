define ([], function () {

    $_DO.choose_tab_overhaul_short_program = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_short_program.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {

        query ({type: 'overhaul_short_programs'}, {}, function (data) {        

            data.vc_gis_status = data.vc_gis_status.map (function (status) {
                if (status['id'] == 40)  return {id: 40,  label: 'размещена'}
                if (status['id'] == 110) return {id: 110, label: 'аннулирована'}
                return status
            })

            add_vocabularies (data, {
                vc_gis_status: 1,
                vc_actions: 1
            })
            
            var it = data.item

            it.not_all_works_approved = (it.id_osp_status == -31 && data.works_approved_count < data.works_general_count) ? true : false

            console.log (data)

            it.status_label = data.vc_gis_status[it.id_osp_status]
            it.start = it.startmonth + '.' + it.startyear
            it.end = it.endmonth + '.' + it.endyear

            it._can = {cancel: 1}

            if (!it.is_deleted && ($_USER.role.admin || it.org_uuid == $_USER.uuid_org)) {

                switch (it.id_osp_status) {
                    case  14:
                    case  34:
                    case 124:
                        it._can.alter = 1
                }

                switch (it.id_osp_status) {
                    case  10:
                    case -31:
                        it._can.approve = 1
                }

                switch (it.id_osp_status) {
                    case 10:
                        it._can.edit = 1
                }

                switch (it.id_osp_status) {
                    case  10:
                    case -34:
                    case -20:
                    case -31:
                    case  14:
                    case -34:
                    case  40:
                    case 104:
                        it._can.delete = 1
                }

                it._can.update = it._can.edit

            }

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})