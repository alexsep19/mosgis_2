define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        it.vc_nsi_20 = data.vc_orgs_nsi_20
            .map (function (r) {return data.vc_nsi_20 [r.code]})
            .sort ()
            .join (',<br>')

        it.vc_organization_types_label = it ['vc_organization_types.label']

        $('title').text (it.label)

        fill (view, it, $('body'))
        
//        $('div.w2ui-field:hidden').remove ()

        $('#the_form').w2reform ({name: 'voc_organization_legal_form', record: it})
                
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
                
        if (it.id_log && it ['out_soap.id_status'] != 3) {
        
            w2utils.lock ($('#the_form'), {
                msg     : 'Ждём ответ ГИС ЖКХ...',       
                spinner : false,    
            })
        
            setTimeout (reload_page, 2000)
        
        }
        
        var charter_uuid = it ['charter.uuid']
        
        if (charter_uuid) {
        
            clickOn ($('div[data-text=stateregistrationdate]'), function () {
                openTab ('/charter/' + charter_uuid)
            })

        }

    }

})