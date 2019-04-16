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

                switch (it.last_succesfull_status) {
                    case  10:
                    case -31:
                        it._can.approve = 1
                }

                switch (it.id_osp_status) {
                    case 10:
                    case 124:
                        it._can.edit = 1
                }

                it._can.update = it._can.edit
                it._can.delete = it._can.approve || (it.id_orp_status == 40)

            }

            $('body').data ('data', data)
    
            done (data)

        }) 
        
    }

})