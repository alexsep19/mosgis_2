define ([], function () {

    return function (data, view) {
    
        data.item.vc_nsi_20 = data.vc_orgs_nsi_20
            .map (function (r) {return data.vc_nsi_20 [r.code]})
            .sort ()
            .join (',<br>')

        data.item.vc_organization_types_label = data.item ['vc_organization_types.label']

        $('title').text (data.item.label)

        fill (view, data.item, $('body'))
        
        $('#the_form').w2reform ({name: 'voc_organization_legal_form'})
        
        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 300,
                
                    tabs: {

                        tabs: [
                            {id: 'voc_organization_legal_users', caption: 'Учётные записи', off: !$_USER.role.admin && $_USER.uuid_org != $_REQUEST.id},
                            {id: 'voc_organization_legal_log', caption: 'История'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_voc_organization_legal

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'voc_organization_legal.active_tab')
            },

        });
                
        if (data.item.id_log && data.item ['out_soap.id_status'] != 3) {
        
            w2utils.lock ($('#the_form'), {
                msg     : 'Ждём ответ ГИС ЖКХ...',       
                spinner : false,    
            })
        
            setTimeout (reload_page, 2000)
        
        }
        
        var charter_uuid = data.item ['charter.uuid']
        
        if (charter_uuid) {
        
            clickOn ($('div[data-text=stateregistrationdate]'), function () {
                openTab ('/charter/' + charter_uuid)
            })
        
//            .css ({color: 'red'})
        
        }

    }

})