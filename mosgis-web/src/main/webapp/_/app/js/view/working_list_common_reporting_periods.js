define ([], function () {

    return function (data, view) {
    
        var columns = [
            {field: 'year', caption: 'Год', size: 10},
        ]
        
        for (var i = 1; i <= 12; i ++) columns.push ({
            field: 'month_' + i, 
            caption: w2utils.settings.fullmonths [i-1], 
            size: 10,
            attr: 'data-ref=1',
            render: function (r, zero, month, uuid) {
                return !uuid ? null : w2utils.settings.shortmonths [month - 1].toLowerCase () + ' ' + r.year
            }
        })
    
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_list_common_reporting_periods_grid',

            show: {
                toolbar: false,
                footer: true,
            },     

            multiSelect: false,

            columns: columns,

            records: data.plans,

            onClick: function (e) {
                var uuid = this.get (e.recid) ['month_' + e.column]
                if (uuid) openTab ('/reporting_period/' + uuid)
            },

        }).refresh ();

    }

})