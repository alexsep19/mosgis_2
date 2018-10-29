define ([], function () {

    var ddt_30 = [];
    for (var i = 1; i <= 30; i ++) ddt_30.push ({id: i, text: i + '-е число'});        
    var ddt_31 = ddt_30.concat ([{id: 31, text: '31-е число'}]);                
    var last = {id: 99, text: 'последнее число'}        
    ddt_30.push (last)
    ddt_31.push (last)
    var ddt_31_from = ddt_31.map (function (i) {return {id: i.id, text: i.text.replace ('е число', 'го числа')}})
    
    var form_name = 'charter_common_form'

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

                if (dt && dt.charAt (3) != '.') r [this.name] = dt_dmy (dt)
                        
            })

            f.record = r
            
            $('div[data-block-name=charter_common] input').prop ({disabled: data.__read_only})
          
            if ($('body').data ('data').item ['vc_orgs.stateregistrationdate']) $('#date_').prop ({disabled: 1})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: data.item.last_termination ? 330 : 290},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'charter_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_charter_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
        
        var nxt = [
            {id: "0", text: "текущего месяца"},
            {id: "1", text: "следующего месяца"},
        ]
        
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'date_', type: 'date'},
                    {name: 'nocharterapproveprotocol', type: 'list', options: {items: [
                        {id: "0", text: "имеется в наличии"},
                        {id: "1", text: "отсутствует"},
                    ]}},                    
                    
                    {name: 'ddt_m_start', type: 'list', options: {items: ddt_31_from}},
                    {name: 'ddt_m_end',   type: 'list', options: {items: ddt_31}},
                    {name: 'ddt_d_start', type: 'list', options: {items: ddt_30}},
                    {name: 'ddt_i_start', type: 'list', options: {items: ddt_30}},

                    {name: 'ddt_m_start_nxt', type: 'list', options: {items: nxt}},
                    {name: 'ddt_m_end_nxt',   type: 'list', options: {items: nxt}},
                    {name: 'ddt_d_start_nxt', type: 'list', options: {items: nxt}},
                    {name: 'ddt_i_start_nxt', type: 'list', options: {items: nxt}},

            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
            
                clickOff ($('#label_org_customer'))
                
                if (data.item.id_ctr_status == 10 && !$('#docnum').prop ('disabled')) clickOn ($('#label_org_customer'), $_DO.open_orgs_charter_common)
                
            })}

        })

        $_F5 (data)        

    }
    
})