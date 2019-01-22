define ([], function () {

    var form_name = 'infrastructure_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]

            f.record = r
            
            $('div[data-block-name=infrastructure_common] input, textarea').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 460},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'infrastructure_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_infrastructure_common
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
                    {name: 'name', type: 'text'},
                    {name: 'manageroki', type: 'hidden'},
                    {name: 'manageroki_label', type: 'text'},
                    {name: 'code_vc_nsi_39', type: 'list', options: {items: data.vc_nsi_39.items}},
                    {name: 'indefinitemanagement', type: 'list', options: {items: [
                        {id: "0", text: "Нет"},
                        {id: "1", text: "Да"},
                    ]}},
                    {name: 'endmanagmentdate', type: 'date'},
                    {name: 'code_vc_nsi_33', type: 'list', options: {items: data.vc_nsi_33.items}},
                    {name: 'independentsource', type: 'list', options: {items: [
                        {id: '0', text: 'Нет'},
                        {id: '1', text: 'Да'}
                    ]}},
                    {name: 'code_vc_nsi_34', type: 'list', options: {items: data.vc_nsi_34.items}},
                    {name: 'code_vc_nsi_35', type: 'list', options: {items: data.vc_nsi_35.items}},
                    {name: 'code_vc_nsi_40', type: 'list', options: {items: data.vc_nsi_40.items}},
                    {name: 'code_vc_nsi_37', type: 'list', options: {items: data.vc_nsi_37.items}},
                    {name: 'code_vc_nsi_38', type: 'list', options: {items: data.vc_nsi_38.items}},
                    {name: 'oktmo', type: 'hidden'},
                    {name: 'oktmo_code', type: 'text'},
                    {name: 'adress', type: 'text'},
                    {name: 'comissioningyear', type: 'text'},
                    {name: 'countaccidents', type: 'text'},
                    {name: 'deterioration', type: 'text'},
                    {name: 'addinfo', type: 'textarea'},

            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {

                clickOn ($('#manageroki_label'), $_DO.open_orgs_infrastructure_popup)
                clickOn ($('#oktmo_code'), $_DO.open_oktmo_popup)

            })}

        })

        var is_virgin = 1

        $('#type_of_utility_container').w2regrid ({ 
        
            name: 'code_vc_nsi_3_grid',
            
            show: {
                toolbar: false,
                footer: false,
                columnHeaders: false,
                selectColumn: true
            },     
            
            columns: [
                {field: 'label', caption: 'Наименование', size: 50},
            ],
            
            records: dia2w2uiRecords (data.vc_nsi_3.items),

            onRefresh: function () {
            
                if (!is_virgin) return
                
                var grid = this
           
                $.each (data.item.codes_nsi_3, function () {grid.select ('' + this)})

                is_virgin = 0

                grid.onSelect = grid.onUnselect = function (e) {

                    if ($('#name').prop ('disabled')) return e.preventDefault ()

                }
            
            }    
        
        }).refresh ()

        $_F5 (data)        

    }
    
})