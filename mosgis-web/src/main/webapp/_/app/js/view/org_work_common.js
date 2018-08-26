define ([], function () {
    
    var form_name = 'org_work_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=org_work_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 370},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'org_work_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_org_work_common
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
                    {name: 'workname', type: 'text'},
                    {name: 'code_vc_nsi_56', type: 'list', options: {items: data.vc_nsi_56.items}},
                    {name: 'stringdimensionunit', type: 'combo', options: {items: data.vc_okei.items}},
            ],

            focus: -1,
            
        })
        
        var is_virgin = 1
        
        $('#required_services_container').w2regrid ({ 
        
            name: 'code_vc_nsi_67_grid',
            
            show: {
                toolbar: false,
                footer: false,
                columnHeaders: false,
                selectColumn: true
            },     
            
            columns: [
                {field: 'label', caption: 'Наименование', size: 50},
            ],
            
            records: dia2w2uiRecords (data.vc_nsi_67.items),
            
            onRefresh: function () {
            
                if (!is_virgin) return
                
                var grid = this
           
                $.each (data.item.codes_nsi_67, function () {grid.select ('' + this)})

                is_virgin = 0
                
                grid.onSelect = grid.onUnselect = function (e) {

                    if ($('#workname').prop ('disabled')) return e.preventDefault ()

                }
            
            }                
        
        }).refresh ()

        $_F5 (data)

    }
    
})