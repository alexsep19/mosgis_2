define ([], function () {
    
    var form_name = 'mgmt_contract_object_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

//            r.label_org_customer = customer_label (vc_gis_customer_type [r.id_customer_type], r ['org_customer.label'])
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=mgmt_contract_object_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 160},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
//                            {id: 'mgmt_contract_object_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_mgmt_contract_object_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
                
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'startdate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item ['ctr.effectivedate']),
                        end:      dt_dmy (data.item ['ctr.plandatecomptetion']),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item ['ctr.effectivedate']),
                        end:      dt_dmy (data.item ['ctr.plandatecomptetion']),
                    }},
                    {name: 'uuid_contract_agreement', type: 'list', options: {items: data.agreements}},
            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
                clickOff ($('#label_org_customer'))
                if (!$('#docnum').prop ('disabled')) clickOn ($('#label_org_customer'), $_DO.open_orgs_mgmt_contract_object_common)            
            })}

        })

        $_F5 (data)        

    }
    
})