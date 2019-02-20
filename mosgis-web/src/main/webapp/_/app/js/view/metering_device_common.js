define ([], function () {
    
    var form_name = 'metering_device_common_form'
    
    function recalc () {
    
        var f = w2ui [form_name]
        var v = f.values ()
        
        $('#span_remote_metering').css ({visibility: v.remotemeteringmode ? 'visible' : 'hidden'})
        $('#span_installationplace').css ({visibility: !v.notlinkedwithmetering ? 'visible' : 'hidden'})
        $('#span_temperaturesensor').css ({visibility: v.temperaturesensor ? 'visible' : 'hidden'})
        $('#span_pressuresensor').css ({visibility: v.pressuresensor ? 'visible' : 'hidden'})

    }

    return function (data, view) {
    
        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
                        
            if (!r.remotemeteringmode) r.remotemeteringmode = 0

            w2ui [form_name].record = r
            
            $('div[data-block-name=metering_device_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 250 + 30 * (it.is_power + it.is_for_building + 2 * it.is_collective)},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'metering_device_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_metering_device_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
        
        var now = dt_dmy (new Date ().toJSON ())

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : it,                
            
            fields : [            
            
                    {name: 'meteringdevicenumber', type: 'text'},
                    {name: 'meteringdevicestamp', type: 'text'},
                    {name: 'meteringdevicemodel', type: 'text'},
                    {name: 'remotemeteringinfo', type: 'text'},
                    {name: 'factorysealdate', type: 'date', options: {end: now}},
                    {name: 'installationdate', type: 'date', options: {end: now}},
                    {name: 'commissioningdate', type: 'date', options: {end: now}},
                    {name: 'firstverificationdate', type: 'date', options: {end: now}},
                    {name: 'remotemeteringmode', type: 'list', options: {items: [
                        {id: 0, text: 'нет'},
                        {id: 1, text: 'возможно'},
                    ]}},
                    {name: 'tariffcount', type: 'int', options: {min:1, max: 3}},
                    {name: 'code_vc_nsi_16', type: 'list', options: {items: data.vc_nsi_16.items}},
                    {name: 'transformationratio', type: 'float', options: {min: 0, precision: 2}},
                    {name: 'notlinkedwithmetering', type: 'list', options: {items: [
                        {id: 1, text: 'нет'},
                        {id: 0, text: 'да'},
                    ]}},
                    {name: 'installationplace', type: 'list', options: {items: data.vc_meter_places.items}},

                    {name: 'temperaturesensor', type: 'list', options: {items: [
                        {id: 0, text: 'нет'},
                        {id: 1, text: 'есть'},
                    ]}},
                    {name: 'temperaturesensingelementinfo', type: 'text'},

                    {name: 'pressuresensor', type: 'list', options: {items: [
                        {id: 0, text: 'нет'},
                        {id: 1, text: 'есть'},
                    ]}},
                    {name: 'pressuresensingelementinfo', type: 'text'},
                    

            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (recalc)},
            
            onChange: function (e) {
            
                if (e.target == "remotemeteringmode") {
                    e.done (function () {recalc (); $('#remotemeteringinfo').focus ()})
                }
                
                if (e.target == "notlinkedwithmetering") {
                    e.done (function () {recalc (); $('#installationplace').focus ()})
                }
                
                if (e.target == "temperaturesensor") {
                    e.done (function () {recalc (); $('#temperaturesensingelementinfo').focus ()})
                }

                if (e.target == "pressuresensor") {
                    e.done (function () {recalc (); $('#pressuresensingelementinfo').focus ()})
                }
            
            },
            
        })

        $_F5 (data)        

    }
    
})