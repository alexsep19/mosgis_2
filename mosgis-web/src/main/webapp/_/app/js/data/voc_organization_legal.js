define ([], function () {

    $_DO.choose_tab_voc_organization_legal = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voc_organization_legal.active_tab', name)
            
        use.block (name)
    }

    $_DO.choose_top_tab_voc_organization_legal = function (e) {

        var name = e.tab.id

        localStorage.setItem('voc_organization_legal.active_top_tab', name)

        use.block(name)
    }

    return function (done) {

        query ({type: 'voc_organizations'}, {}, function (data) {
            
            data.nsi_20 = {}
        
            $.each (data.vc_orgs_nsi_20, function () {data.nsi_20 [this.code] = 1})
            
            if ($_USER.role.admin) {
            
                var is_coop = false
                for (var i = 19; i <= 22; i ++) is_coop |= data.nsi_20 [i]

                data.import_types = [

                    {id: "import_mgmt_contracts", text: "Импорт договоров управления", off: !data.nsi_20 [1]},
                    {id: "import_mgmt_charters", text: "Импорт устава", off: !is_coop},
                    {id: "import_add_services",   text: "Импорт справочника дополнительных услуг", off: !data.nsi_20 [1] && !is_coop},

                ].filter (not_off)
                
            }
            
            add_vocabularies (data, {
                vc_acc_req_status: 1,
                vc_acc_req_types: 1,
                vc_actions: 1,
            })
            
            var it = data.item
            
            it._can = {
                refresh: 1,
                import: data.import_types
            }

            $('body').data ('data', data)

            get_nsi([20], done)

        })

    }

})