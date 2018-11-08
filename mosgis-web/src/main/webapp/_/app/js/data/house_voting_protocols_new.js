define ([], function () {

    $_DO.update_house_voting_protocols_new = function (e) {

        var form = w2ui ['house_voting_protocols_new_form']

        var v = form.values ()
        
        if (!v.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!v.hasOwnProperty('extravoting')) die ('extravoting', 'Пожалуйста, укажите вид собрания')
        if (!v.hasOwnProperty('meetingeligibility')) die ('meetingeligibility', 'Пожалуйста, укажите правомочность собрания')
        if (!v.hasOwnProperty('form_')) die ('form_', 'Пожалуйста, выберите форму проведения')
        
        switch (v.form_) {
            case 0:
                if (!v.avotingdate) die ('avotingdate', 'Пожалуйста, введите дату окончания приема решений')
                if (!v.resolutionplace) die ('resolutionplace', 'Пожалуйста, введите место принятия решений')
                break;
            case 1:
                if (!v.meetingdate) die ('meetingdate', 'Пожалуйста, введите дату проведения собрания')
                if (!v.meetingtime) die ('meetingtime', 'Пожалуйста, введите время проведения собрания')
                if (!v.votingplace) die ('votingplace', 'Пожалуйста, введите место проведения собрания')
                v.meetingdate = v.meetingdate + " " + v.meetingtime + ":00"
                break;
            case 2:
                if (!v.evotingdatebegin) die ('evotingdatebegin', 'Пожалуйста, введите дату начала проведения голосования')
                if (!v.evotingtimebegin) die ('evotingtimebegin', 'Пожалуйста, введите время начала проведения голосования')
                if (!v.evotingdateend) die ('evotingdateend', 'Пожалуйста введите дату окончания проведения голосования')
                if (!v.evotingtimeend) die ('evotingtimeend', 'Пожалуйста, введите время окончания проведения голосования')
                if (!v.discipline) die ('discipline', 'Пожалуйста, введите порядок приема оформленных в письменной форме решений собственников')
                if (!v.inforeview) die ('inforeview', 'Пожалуйста, введите порядок ознакомления с информацией и (или) материалами, которые будут представлены на данном собрании')
                
                v.evotingdatebegin = v.evotingdatebegin + " " + v.evotingtimebegin + ":00"
                v.evotingdateend = v.evotingdateend + " " + v.evotingtimeend + ":00"
                
                if ((Date.parse (v.evotingdateend) - Date.parse (v.evotingdateend)) <= 0) die ('evotingdateend', 'Некорректный временной промежуток')
                break;
            case 3:
                if (!v.meeting_av_date) die ('meeting_av_date', 'Пожалуйста, введите дату проведения собрания')
                if (!v.meeting_av_time) die ('meeting_av_time', 'Пожалуйста, введите время проведения собрания')
                if (!v.meeting_av_date_end) die ('meeting_av_date_end', 'Пожалуйста, введите дату окончания приема решений')
                if (!v.meeting_av_res_place) die ('meeting_av_res_place', 'Пожалуйста, введите место приема решения')
                
                if ((Date.parse (v.meeting_av_date_end) - Date.parse (v.meeting_av_date)) < 0) die ('evotingdateend', 'Некорректный временной промежуток')
                
                v.meeting_av_date = v.meeting_av_date + " " + v.meeting_av_time + ":00"
                break;
        }
        
        v['meetingeligibility'] = v['meetingeligibility'] == 0 ? "C" : "N"
        
        var tia = {type: 'voting_protocols'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['house_voting_protocols_grid']
        
        var data = clone ($('body').data ('data'))
        
        v['fiashouseguid'] = data.item.fiashouseguid

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу протокола?').yes (function () {openTab ('/voting_protocol/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})