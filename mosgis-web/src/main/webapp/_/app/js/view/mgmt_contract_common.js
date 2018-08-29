define ([], function () {
    
    var form_name = 'mgmt_contract_common_form'

    function customer_label (type, org) {
        return org ? org + ' (' + type + ')': type
    }
    
    var vc_gis_customer_type

    return function (data, view) {
    
        vc_gis_customer_type = data.vc_gis_customer_type
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            r.label_org_customer = customer_label (vc_gis_customer_type [r.id_customer_type], r ['org_customer.label'])
            
            var f = w2ui [form_name]
            
            $.each (f.fields, function () {
            
                if (this.type != 'date') return
                
                var dt = r [this.name]
                
                if (dt.charAt (3) != '.') r [this.name] = dt_dmy (dt)
                        
            })

            f.record = r
            
            $('div[data-block-name=mgmt_contract_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 350},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
//                            {id: 'mgmt_contract_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_mgmt_contract_common
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
                    {name: 'docnum', type: 'text'},
                    {name: 'signingdate', type: 'date'},
                    {name: 'effectivedate', type: 'date'},
                    {name: 'plandatecomptetion', type: 'date'},
                    {name: 'code_vc_nsi_58', type: 'list', options: {items: data.vc_nsi_58.items}},
                    {name: 'automaticrolloveroneyear', type: 'list', options: {items: [
                        {id: "0", text: "нет"},
                        {id: "1", text: "на 1 год при наступлении даты окончания"},
                    ]}},                    
                    {name: 'vc_orgs.label', type: 'text'},
                    {name: 'uuid_org_customer', type: 'hidden'},
                    {name: 'label_org_customer', type: 'text'},
            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
                clickOff ($('#label_org_customer'))
                if (!$('#docnum').prop ('disabled')) clickOn ($('#label_org_customer'), $_DO.open_orgs_mgmt_contract_common)            
            })}

        })

        $_F5 (data)        

    }
    
})