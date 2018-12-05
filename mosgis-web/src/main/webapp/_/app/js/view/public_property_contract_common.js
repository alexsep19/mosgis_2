define ([], function () {
    
    var form_name = 'public_property_contract_common_form'
    
    function recalc () {
    
        var f = w2ui [form_name]
        
        var v = f.values ()
        
        var i = v.isgratuitousbasis
                
        $('#td_other'    ).css ({display: i == -1 ? 'table-cell' : 'none'})
        $('#tr_ddt_start').css ({display: i ==  0 ? 'table-row' : 'none'})
        $('#tr_ddt_end'  ).css ({display: i ==  0 ? 'table-row' : 'none'})
        $('#tr_payment'  ).css ({display: i !=  1 ? 'table-row' : 'none'})        
        
        var hidden = 
            i ==  0 ? 0:
            i == -1 ? 2:
                      3
        
        var s = 440 - hidden * 30
        var l = w2ui ['passport_layout']
        var t = l.get ('top')
        if (t.size != s) l.set ('top', {size: s})
    
    }

    return function (data, view) {

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]                       

            f.record = r
            
            $('div[data-block-name=public_property_contract_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 440},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'public_property_contract_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_public_property_contract_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
                
        var ddt_30 = [];
        for (var i = 1; i <= 30; i ++) ddt_30.push ({id: i, text: i + '-е число'});        
        var ddt_31 = ddt_30.concat ([{id: 31, text: '31-е число'}]);                
        var last = {id: 99, text: 'последнее число'}        
        ddt_30.push (last)
        ddt_31.push (last)
        var ddt_31_from = ddt_31.map (function (i) {return {id: i.id, text: i.text.replace ('е число', 'го числа')}})

        var nxt = [
            {id: "0", text: "текущего месяца"},
            {id: "1", text: "следующего месяца"},
        ]

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                {name: 'customer_label', type: 'text'},
                {name: 'org_label', type: 'text'},
                {name: 'contractnumber', type: 'text'},
                {name: 'date_', type: 'date'},
                {name: 'enddate', type: 'date'},
                {name: 'startdate', type: 'date'},
                {name: 'contractobject', type: 'text'},
                {name: 'comments', type: 'text'},
                {name: 'other', type: 'text'},
                {name: 'moneyspentdirection', type: 'text'},
                {name: 'payment', type: 'float:2'},
                {name: 'isgratuitousbasis', type: 'list', options: {items:[
                    {id: 1,  text: 'Безвозмездное'},
                    {id: 0,  text: 'С ежемесячной оплатой'}, 
                    {id: -1, text: 'С иной оплатой'}, 
                ]}},
                {name: 'ddt_start', type: 'list', options: {items: ddt_31_from}},
                {name: 'ddt_end',   type: 'list', options: {items: ddt_31}},
                {name: 'ddt_start_nxt', type: 'list', options: {items: nxt}},
                {name: 'ddt_end_nxt',   type: 'list', options: {items: nxt}},
            ],

            focus: -1,

            onRefresh: function (e) {e.done (recalc)},
            onChange:  function (e) {if (e.target='isgratuitousbasis') e.done (recalc)},

        })

        $_F5 (data)        

    }
    
})